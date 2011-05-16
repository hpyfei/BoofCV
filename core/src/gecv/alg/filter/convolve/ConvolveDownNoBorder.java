/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.filter.convolve;

import gecv.alg.filter.convolve.down.*;
import gecv.struct.convolve.Kernel1D_F32;
import gecv.struct.convolve.Kernel1D_I32;
import gecv.struct.convolve.Kernel2D_F32;
import gecv.struct.convolve.Kernel2D_I32;
import gecv.struct.image.*;


/**
 * <p>
 * Specialized convolution where the center of the convolution skips over a constant number
 * of pixels in the x and/or y axis.  The output it written into an image in a dense fashion,
 * resulting in it being at a lower resolution.  A typical application for this is down sampling
 * inside an image pyramid.
 * </p>
 * 
 * @author Peter Abeles
 */
public class ConvolveDownNoBorder {

	public static void horizontal( Kernel1D_F32 kernel , ImageFloat32 input, ImageFloat32 output , int skip ) {
		if( !ConvolveDownNoBorderUnrolled_F32_F32.horizontal(kernel,input,output,skip)) {
			ConvolveDownNoBorderStandard.horizontal(kernel,input,output,skip);
		}
	}

	public static void vertical( Kernel1D_F32 kernel , ImageFloat32 input, ImageFloat32 output , int skip ) {
		if( !ConvolveDownNoBorderUnrolled_F32_F32.vertical(kernel,input,output,skip)) {
			ConvolveDownNoBorderStandard.vertical(kernel,input,output,skip);
		}
	}

	public static void convolve( Kernel2D_F32 kernel , ImageFloat32 input, ImageFloat32 output , int skip ) {
		if( !ConvolveDownNoBorderUnrolled_F32_F32.convolve(kernel,input,output,skip)) {
			ConvolveDownNoBorderStandard.convolve(kernel,input,output,skip);
		}
	}

	public static void horizontal( Kernel1D_I32 kernel , ImageUInt8 input, ImageInt16 output , int skip ) {
		if( !ConvolveDownNoBorderUnrolled_U8_I16.horizontal(kernel,input,output,skip)) {
			ConvolveDownNoBorderStandard.horizontal(kernel,input,output,skip);
		}
	}

	public static void vertical( Kernel1D_I32 kernel , ImageUInt8 input, ImageInt16 output , int skip ) {
		if( !ConvolveDownNoBorderUnrolled_U8_I16.vertical(kernel,input,output,skip)) {
			ConvolveDownNoBorderStandard.vertical(kernel,input,output,skip);
		}
	}

	public static void convolve( Kernel2D_I32 kernel , ImageUInt8 input, ImageInt16 output , int skip ) {
		if( !ConvolveDownNoBorderUnrolled_U8_I16.convolve(kernel,input,output,skip)) {
			ConvolveDownNoBorderStandard.convolve(kernel,input,output,skip);
		}
	}

	public static void horizontal( Kernel1D_I32 kernel , ImageSInt16 input, ImageInt16 output , int skip ) {
		if( !ConvolveDownNoBorderUnrolled_S16_I16.horizontal(kernel,input,output,skip)) {
			ConvolveDownNoBorderStandard.horizontal(kernel,input,output,skip);
		}
	}

	public static void vertical( Kernel1D_I32 kernel , ImageSInt16 input, ImageInt16 output , int skip ) {
		if( !ConvolveDownNoBorderUnrolled_S16_I16.vertical(kernel,input,output,skip)) {
			ConvolveDownNoBorderStandard.vertical(kernel,input,output,skip);
		}
	}

	public static void convolve( Kernel2D_I32 kernel , ImageSInt16 input, ImageInt16 output , int skip ) {
		if( !ConvolveDownNoBorderUnrolled_S16_I16.convolve(kernel,input,output,skip)) {
			ConvolveDownNoBorderStandard.convolve(kernel,input,output,skip);
		}
	}

	public static void horizontal( Kernel1D_I32 kernel , ImageUInt8 input, ImageInt8 output , int skip , int divisor ) {
		if( !ConvolveDownNoBorderUnrolled_U8_I8_Div.horizontal(kernel,input,output,skip,divisor)) {
			ConvolveDownNoBorderStandard.horizontal(kernel,input,output,skip,divisor);
		}
	}

	public static void vertical( Kernel1D_I32 kernel , ImageUInt8 input, ImageInt8 output , int skip , int divisor ) {
		if( !ConvolveDownNoBorderUnrolled_U8_I8_Div.vertical(kernel,input,output,skip,divisor)) {
			ConvolveDownNoBorderStandard.vertical(kernel,input,output,skip,divisor);
		}
	}

	public static void convolve( Kernel2D_I32 kernel , ImageUInt8 input, ImageInt8 output , int skip , int divisor ) {
		if( !ConvolveDownNoBorderUnrolled_U8_I8_Div.convolve(kernel,input,output,skip,divisor)) {
			ConvolveDownNoBorderStandard.convolve(kernel,input,output,skip,divisor);
		}
	}

	public static void horizontal( Kernel1D_I32 kernel , ImageSInt16 input, ImageInt16 output , int skip , int divisor ) {
		if( !ConvolveDownNoBorderUnrolled_S16_I16_Div.horizontal(kernel,input,output,skip,divisor)) {
			ConvolveDownNoBorderStandard.horizontal(kernel,input,output,skip,divisor);
		}
	}

	public static void vertical( Kernel1D_I32 kernel , ImageSInt16 input, ImageInt16 output , int skip , int divisor ) {
		if( !ConvolveDownNoBorderUnrolled_S16_I16_Div.vertical(kernel,input,output,skip,divisor)) {
			ConvolveDownNoBorderStandard.vertical(kernel,input,output,skip,divisor);
		}
	}

	public static void convolve( Kernel2D_I32 kernel , ImageSInt16 input, ImageInt16 output , int skip , int divisor ) {
		if( !ConvolveDownNoBorderUnrolled_S16_I16_Div.convolve(kernel,input,output,skip,divisor)) {
			ConvolveDownNoBorderStandard.convolve(kernel,input,output,skip,divisor);
		}
	}
}