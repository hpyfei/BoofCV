/*
 * Copyright (c) 2011-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.fiducial.qrcode;

import org.ddogleg.struct.GrowQueue_I32;
import org.ddogleg.struct.GrowQueue_I8;

import java.util.Arrays;

/**
 * TODO Summarize
 *
 * <p>Code and code comments based on the tutorial at [1].</p>
 *
 *  <p>[1] <a href="https://en.wikiversity.org/wiki/Reed–Solomon_codes_for_coders">Reed-Solomon Codes for Coders</a>
 *  Viewed on September 28, 2017</p>
 *
 * @author Peter Abeles
 */
public class ReidSolomonCodes {

	GaliosFieldTableOps math;

	GrowQueue_I8 generator = new GrowQueue_I8();

	GrowQueue_I8 tmp0 = new GrowQueue_I8();
	GrowQueue_I8 tmp1 = new GrowQueue_I8();

	GrowQueue_I32 errorLocations = new GrowQueue_I32();
	GrowQueue_I8 errorLocatorPoly = new GrowQueue_I8();
	GrowQueue_I32 syndromes = new GrowQueue_I32();

	public ReidSolomonCodes( int numBits , int primitive) {
		math = new GaliosFieldTableOps(numBits,primitive);
	}

	public void setDegree( int degree ) {
		generator(degree);
	}

	/**
	 * Given the input message compute the error correction code for it
	 * @param input Input message. Modified internally then returned to its initial state
	 * @param output error correction code
	 */
	public void computeECC( GrowQueue_I8 input , GrowQueue_I8 output ) {

		int N = generator.size-1;
		input.extend(input.size+N);
		Arrays.fill(input.data,input.size-N,input.size,(byte)0);

		math.polyDivide(input,generator,tmp0,output);

		input.size -= N;
	}

	/**
	 * Decodes the message and performs any neccisary error correction
	 * @param input (Input) Message
	 * @param ecc (Input) error correction code for the message
	 * @param output (Output) the error corrected message
	 * @return true if it was successful or false if it failed
	 */
	public boolean decode( GrowQueue_I8 input ,
						   GrowQueue_I8 ecc,
						   GrowQueue_I8 output )
	{
		syndromes.resize(syndromeLength());

		computeSyndromes(input,ecc,syndromes.data);
		findErrorLocatorBM(syndromes.data,syndromes.size,errorLocatorPoly);
		if( !findErrorLocations_BruteForce(errorLocatorPoly,input.size+ecc.size,errorLocations))
			return false;

		// todo output goes in output?
		correctErrors(input,syndromes.data,errorLocations);
		return true;
	}

	/**
	 * Computes the syndromes for the message (input + ecc). If there's no error then the output will be zero.
	 * @param input Data portion of the message
	 * @param ecc ECC portion of the message
	 * @param syndromes (Output) results of the syndromes computations
	 */
	void computeSyndromes( GrowQueue_I8 input ,
						   GrowQueue_I8 ecc ,
						   int syndromes[])
	{
		int N = syndromeLength();
		for (int i = 0; i < N; i++) {
			int val = math.power(2,i);
			syndromes[i] = math.polyEval(input,val);
			syndromes[i] = math.polyEvalContinue(syndromes[i],ecc,val);
		}
	}

	/**
	 * Computes the error locator polynomial using  Berlekamp-Massey algorithm [1]
	 *
	 * <p>[1] Massey, J. L. (1969), "Shift-register synthesis and BCH decoding" (PDF), IEEE Trans.
	 * Information Theory, IT-15 (1): 122–127</p>
	 *
	 * @param syndromes The syndromes
	 * @param length number of elements in syndromes
	 * @param errorLocator (Output) the error locator polynomial. Coefficients go for small to large.
	 */
	void findErrorLocatorBM( int syndromes[] , int length , GrowQueue_I8 errorLocator ) {
		GrowQueue_I8 C = errorLocator; // error polynomial
		GrowQueue_I8 B = new GrowQueue_I8();  // previous error polynomial
		// TODO remove new from this function

		initToOne(C,length+1);
		initToOne(B,length+1);

		GrowQueue_I8 tmp = new GrowQueue_I8(length);

//		int L = 0;
//		int m = 1; // stores how much B is 'shifted' by
		int b = 1;

		for (int n = 0; n < length; n++) {

			// Compute discrepancy delta
			int delta = syndromes[n];

			for (int j = 1; j < C.size; j++) {
				delta ^= math.multiply(C.data[C.size-j-1]&0xFF, syndromes[n-j]);
			}

			// B = D^m * B
			B.data[B.size++] = 0;

			// Step 3 is implicitly handled
			// m = m + 1

			if( delta != 0 ) {
				int scale = math.multiply(delta, math.inverse(b));
				math.polyAddScaleB(C, B, scale, tmp);

				if (2 * C.size > length) {
					// if 2*L > N ---- Step 4
//					m += 1;
				} else {
					// if 2*L <= N --- Step 5
					B.setTo(C);
//					L = n+1-L;
					b = delta;
//					m = 1;
				}
				C.setTo(tmp);
			}
		}

		removeLeadingZeros(C);
	}

	private void removeLeadingZeros(GrowQueue_I8 poly ) {
		int count = 0;
		for (; count < poly.size; count++) {
			if( poly.data[count] != 0 )
				break;
		}
		for (int i = count; i < poly.size; i++) {
			poly.data[i-count] = poly.data[i];
		}
		poly.size -= count;
	}

	/**
	 * Creates a list of bytes that have errors in them
	 *
	 * @param errorLocator Error locator polynomial. Coefficients from small to large.
	 * @param messageLength Length of the message + ecc.
	 * @param locations (Output) locations of bytes in message with errors.
	 */
	public boolean findErrorLocations_BruteForce(GrowQueue_I8 errorLocator ,
												 int messageLength ,
												 GrowQueue_I32 locations )
	{
		locations.resize(0);
		for (int i = 0; i < messageLength; i++) {
			if( math.polyEval_S(errorLocator,math.power(2,i)) == 0 ) {
				locations.add(messageLength-i-1);
			}
		}

		// see if the expected number of errors were found
		return locations.size == errorLocator.size - 1;
	}

	public void correctErrors( GrowQueue_I8 message , int syndromes[], GrowQueue_I32 locations )
	{

	}

	/**
	 * Creates the generator function with the specified polynomial degree. The generator function is composed
	 * of factors of (x-a_n) where a_n is a power of 2.<br>
	 *
	 * g<sub>4</sub>(x) = (x - α0) (x - α1) (x - α2) (x - α3) = 01 x4 + 0f x3 + 36 x2 + 78 x + 40
	 */
	void generator( int degree ) {
		// initialize to a polynomial = 1
		initToOne(generator,degree+1);

		// (1*x - a[i])
		tmp1.resize(2);
		tmp1.data[0] = 1;
		for (int i = 0; i < degree; i++) {
			tmp1.data[1] = (byte)math.power(2,i);
			math.polyMult(generator,tmp1,tmp0);
			generator.setTo(tmp0);
		}
	}

	void initToOne( GrowQueue_I8 poly , int length ) {
		poly.setMaxSize(length);
		poly.size = 1;
		poly.data[0] = 1;
	}

	private int syndromeLength() {
		return generator.size-1;
	}
}
