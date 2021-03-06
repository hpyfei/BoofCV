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

package boofcv.alg.shapes.polyline.keyline;

import boofcv.misc.CircularIndex;
import georegression.metric.Distance2D_I32;
import georegression.struct.line.LineParametric2D_I32;
import georegression.struct.point.Point2D_I32;
import org.ddogleg.struct.GrowQueue_F64;
import org.ddogleg.struct.GrowQueue_I32;

import java.util.List;

/**
 * Detects points inside a contour which are potential corner points
 *
 * TODO describe in more detail
 *
 * @author Peter Abeles
 */
public class ContourInterestPointDetectorLoop {

	// which indexes in the contour are local maximums
	GrowQueue_I32 indexes = new GrowQueue_I32();
	// the corner intensity of each pixel in the contour
	GrowQueue_F64 intensity = new GrowQueue_F64();
	// seperation along the contour
	int period = 0;

	// Work space for used to compute corner intensity
	LineParametric2D_I32 line = new LineParametric2D_I32();

	// threshold for rejecting corners as a maximum
	double threshold;

	public ContourInterestPointDetectorLoop(int period, double threshold) {
		this.period = period;
		this.threshold = threshold;
	}

	public void process(List<Point2D_I32> contour ) {
		if( contour.size() < period )
			throw new RuntimeException("Contour is too small. Must be at least the period or even better more!");
		computeCornerIntensity(contour);
		nonMaximumSupression(contour);
	}

	private void nonMaximumSupression(List<Point2D_I32> contour) {
		indexes.reset();

		int centerOffset = period/2;

		for (int i = 0; i < contour.size(); i++) {
			int target = (i+centerOffset)%contour.size();
			double value = intensity.get(target);

			if( value <= threshold )
				continue;

			boolean maximum = false;
			for (int j = 0; j < period; j++) {
				int k = CircularIndex.addOffset(i,j,contour.size());

				if( intensity.get(k) >= value ) {
					if( k != target ) {
						maximum = true;
						break;
					}
				}
			}

			if( maximum ) {
				indexes.add(target);
			}
		}
	}

	private void computeCornerIntensity(List<Point2D_I32> contour) {
		intensity.resize(contour.size());
		int centerOffset = period/2;

		for (int i = 0; i < contour.size(); i++) {
			int j = (i+period)%contour.size();
			int center = (i+centerOffset)%contour.size();

			Point2D_I32 a = contour.get(i);
			Point2D_I32 b = contour.get(center);
			Point2D_I32 c = contour.get(j);

			line.p.set(a);
			line.slopeX = c.x-a.x;
			line.slopeY = c.y-a.y;

			// not sure if this is even possible...
			if( line.slopeX == 0 && line.slopeY == 0 ) {
				intensity.data[center] = 0;
			} else {
				intensity.data[center] = Distance2D_I32.distance(line,b);
			}
		}
	}

	/**
	 * Indexes of interest points
	 * @return interest points
	 */
	public GrowQueue_I32 getIndexes() {
		return indexes;
	}

	/**
	 * Points all the interest points into a list.
	 * @param contour (Input) The original contour. Not modified.
	 * @param output (Output) Only the interest points from the contour. Modified.
	 */
	public void getInterestPoints( List<Point2D_I32> contour , List<Point2D_I32> output ) {
		output.clear();
		for (int i = 0; i < indexes.size; i++) {
			output.add( contour.get( indexes.get(i)));
		}
		output.clear();
	}
}
