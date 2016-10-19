/*
 * Copyright (c) 2011-2016, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.distort;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestWideToNarrowPtoP_F64 {
	/**
	 * With no translation request a point in the center.  Should appear to be in the center in both views.
	 */
	@Test
	public void centerIsCenter() {
		fail("Implement");
	}

	/**
	 * Rotate the camera and see if the point moves in the expected way
	 */
	@Test
	public void rotateCamera() {
		fail("Implement");
	}

	/**
	 * Request points at the border and see if it has the expected vertical and horizontal FOV
	 */
	@Test
	public void checkFOVBounds() {
		fail("Implement");
	}
}