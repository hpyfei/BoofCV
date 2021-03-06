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

package boofcv.examples.stereo;

import boofcv.abst.distort.FDistort;
import boofcv.alg.geo.PerspectiveOps;
import boofcv.alg.geo.rectify.RectifyCalibrated;
import boofcv.gui.d3.PointCloudViewer;
import boofcv.gui.image.ShowImages;
import boofcv.gui.image.VisualizeImageData;
import boofcv.io.UtilIO;
import boofcv.io.calibration.CalibrationIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.calib.StereoParameters;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import georegression.geometry.GeometryMath_F64;
import georegression.struct.point.Point3D_F64;
import org.ejml.data.DMatrixRMaj;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Expanding upon ExampleStereoDisparity, this example demonstrates how to rescale an image for stereo processing and
 * then compute its 3D point cloud.  Images are often rescaled to improve speed and some times quality.  Creating
 * 3D point clouds from disparity images is easy and well documented in the literature, but there are some nuances
 * to it.
 *
 * @author Peter Abeles
 */
public class ExampleStereoDisparity3D {

	// Specifies what size input images are scaled to
	public static final double scale = 0.5;

	// Specifies what range of disparity is considered
	public static final int minDisparity = 0;
	public static final int maxDisparity = 40;
	public static final int rangeDisparity = maxDisparity-minDisparity;

	public static void main( String args[] ) {
		// ------------- Compute Stereo Correspondence

		// Load camera images and stereo camera parameters
		String calibDir = UtilIO.pathExample("calibration/stereo/Bumblebee2_Chess/");
		String imageDir = UtilIO.pathExample("stereo/");

		StereoParameters param = CalibrationIO.load(new File(calibDir , "stereo.yaml"));

		// load and convert images into a BoofCV format
		BufferedImage origLeft = UtilImageIO.loadImage(imageDir , "chair01_left.jpg");
		BufferedImage origRight = UtilImageIO.loadImage(imageDir , "chair01_right.jpg");

		GrayU8 distLeft = ConvertBufferedImage.convertFrom(origLeft, (GrayU8) null);
		GrayU8 distRight = ConvertBufferedImage.convertFrom(origRight,(GrayU8)null);

		// re-scale input images
		GrayU8 scaledLeft = new GrayU8((int)(distLeft.width*scale),(int)(distLeft.height*scale));
		GrayU8 scaledRight = new GrayU8((int)(distRight.width*scale),(int)(distRight.height*scale));

		new FDistort(distLeft,scaledLeft).scaleExt().apply();
		new FDistort(distRight,scaledRight).scaleExt().apply();

		// Don't forget to adjust camera parameters for the change in scale!
		PerspectiveOps.scaleIntrinsic(param.left, scale);
		PerspectiveOps.scaleIntrinsic(param.right,scale);

		// rectify images and compute disparity
		GrayU8 rectLeft = new GrayU8(scaledLeft.width,scaledLeft.height);
		GrayU8 rectRight = new GrayU8(scaledRight.width,scaledRight.height);

		RectifyCalibrated rectAlg = ExampleStereoDisparity.rectify(scaledLeft,scaledRight,param,rectLeft,rectRight);

//		GrayU8 disparity = ExampleStereoDisparity.denseDisparity(rectLeft, rectRight, 3,minDisparity, maxDisparity);
		GrayF32 disparity = ExampleStereoDisparity.denseDisparitySubpixel(rectLeft, rectRight, 3, minDisparity, maxDisparity);

		// ------------- Convert disparity image into a 3D point cloud

		// The point cloud will be in the left cameras reference frame
		DMatrixRMaj rectK = rectAlg.getCalibrationMatrix();
		DMatrixRMaj rectR = rectAlg.getRectifiedRotation();

		// used to display the point cloud
		PointCloudViewer viewer = new PointCloudViewer(rectK, 10);
		viewer.setPreferredSize(new Dimension(rectLeft.width,rectLeft.height));

		// extract intrinsic parameters from rectified camera
		double baseline = param.getBaseline();
		double fx = rectK.get(0,0);
		double fy = rectK.get(1,1);
		double cx = rectK.get(0,2);
		double cy = rectK.get(1,2);

		// Iterate through each pixel in disparity image and compute its 3D coordinate
		Point3D_F64 pointRect = new Point3D_F64();
		Point3D_F64 pointLeft = new Point3D_F64();
		for( int y = 0; y < disparity.height; y++ ) {
			for( int x = 0; x < disparity.width; x++ ) {
				double d = disparity.unsafe_get(x,y) + minDisparity;

				// skip over pixels were no correspondence was found
				if( d >= rangeDisparity )
					continue;

				// Coordinate in rectified camera frame
				pointRect.z = baseline*fx/d;
				pointRect.x = pointRect.z*(x - cx)/fx;
				pointRect.y = pointRect.z*(y - cy)/fy;

				// rotate into the original left camera frame
				GeometryMath_F64.multTran(rectR, pointRect, pointLeft);

				// add pixel to the view for display purposes and sets its gray scale value
				int v = rectLeft.unsafe_get(x, y);
				viewer.addPoint(pointLeft.x, pointLeft.y, pointLeft.z, v << 16 | v << 8 | v);
			}
		}

		// display the results.  Click and drag to change point cloud camera
		BufferedImage visualized = VisualizeImageData.disparity(disparity, null,minDisparity, maxDisparity,0);
		ShowImages.showWindow(visualized,"Disparity");
		ShowImages.showWindow(viewer,"Point Cloud");
	}
}
