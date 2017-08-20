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

package boofcv.alg.shapes.polygon;

import georegression.struct.point.Point2D_I32;
import georegression.struct.shapes.Polygon2D_F64;
import georegression.struct.shapes.RectangleLength2D_I32;
import org.ddogleg.struct.GrowQueue_I32;
import org.ejml.UtilEjml;
import org.junit.Test;

import java.util.List;

import static boofcv.alg.shapes.polygon.CommonFitPolygonChecks.checkPolygon;
import static boofcv.alg.shapes.polygon.TestContourEdgeIntensity.computeContourVertexes;
import static boofcv.alg.shapes.polygon.TestContourEdgeIntensity.rectToContour;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestRefinePolygonToContour {
	@Test
	public void fitLinesToContour_basic() {
		RectangleLength2D_I32 rect = new RectangleLength2D_I32(0,0,10,5);
		List<Point2D_I32> contour = rectToContour(rect);
		GrowQueue_I32 vertexes = computeContourVertexes(rect);
		RefinePolygonToContour alg = new RefinePolygonToContour();

		Polygon2D_F64 found = new Polygon2D_F64();
		alg.fitLinesToContour(contour,vertexes,found);
		assertTrue(checkPolygon(new double[]{0,0, 9,0, 9,4, 0,4},found));
	}

	@Test
	public void fitLinesToContour_reverseOrder() {
		RectangleLength2D_I32 rect = new RectangleLength2D_I32(0,0,10,5);
		List<Point2D_I32> contour = rectToContour(rect);
		GrowQueue_I32 vertexes = computeContourVertexes(rect);

		flip(vertexes.data,vertexes.size);

		RefinePolygonToContour alg = new RefinePolygonToContour();

		Polygon2D_F64 found = new Polygon2D_F64();
		alg.fitLinesToContour(contour,vertexes,found);
		assertTrue(checkPolygon(new double[]{0,0, 0,4, 9,4, 9,0},found));
	}

	@Test
	public void adjustForThresholdBias() {
		RefinePolygonToContour alg = new RefinePolygonToContour();

		Polygon2D_F64 original = new Polygon2D_F64(10,10, 10,40, 35,40, 35,10);
		Polygon2D_F64 shape = original.copy();
		alg.adjustForThresholdBias(shape,true);

		assertTrue( shape.get(0).distance(10,10) <= UtilEjml.EPS );
		assertTrue( shape.get(1).distance(10,41) <= UtilEjml.EPS );
		assertTrue( shape.get(2).distance(36,41) <= UtilEjml.EPS );
		assertTrue( shape.get(3).distance(36,10) <= UtilEjml.EPS );

		shape = original.copy();
		shape.flip();

		alg.adjustForThresholdBias(shape,false);

		assertTrue( shape.get(0).distance(10,10) <= UtilEjml.EPS );
		assertTrue( shape.get(3).distance(10,41) <= UtilEjml.EPS );
		assertTrue( shape.get(2).distance(36,41) <= UtilEjml.EPS );
		assertTrue( shape.get(1).distance(36,10) <= UtilEjml.EPS );
	}

	//TODO move to ddogleg? primitive flip
	public static void flip( int []a , int N ) {
		int H = N/2;

		for (int i = 0; i < H; i++) {
			int j = N-i-1;
			int tmp = a[i];
			a[i] = a[j];
			a[j] = tmp;
		}
	}

}