package endrov.worms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.vecmath.Vector2d;

import com.graphbuilder.curve.CardinalSpline;
import com.graphbuilder.curve.Point;

import endrov.tesselation.PolygonRasterizer;
import endrov.tesselation.utils.Line;
import endrov.util.ImVector2;
import endrov.util.Vector2i;
import endrov.util.curves.EvCardinalSpline;
import endrov.worms.skeleton.SkeletonTransform;
import endrov.worms.skeleton.SkeletonUtils;
import endrov.worms.skeleton.WormClusterSkeleton;
import endrov.worms.skeleton.WormSkeleton;

public class WormProfile
	{
		public WormPixelMatcher wpm;	
		public int[] thickness;
				
		public WormProfile(ArrayList<WormSkeleton> worms,boolean consecPts,int numPoints,int[] dtArray){
			WormProfile tempProf;
			int numWorms = worms.size();
			int[][] thicknessList = new int[numWorms][numPoints];
			Iterator<WormSkeleton> wit = worms.iterator();
			int count=0;
			while(wit.hasNext()){
				tempProf = new WormProfile(wit.next(),consecPts,numPoints,dtArray);								
				thicknessList[count] = tempProf.thickness;
				count++;
			}
			wpm = worms.get(0).getPixelMatcher();
			thickness= new int[numPoints];
			//Set the average thickness for each control point
			for(int i=1;i<numPoints-2;i++){
				int average = 0;
				for(int j=0;j<numWorms;j++){
					average+= thicknessList[j][i];
				}
				thickness[i] = (int)(Math.round((double)average/(double)numWorms));
			}		
		}
		

		public WormProfile(WormSkeleton ws, boolean consecPts,int numPoints,int[] dtArray){
			this.wpm = ws.getPixelMatcher();
			
			//ArrayList<Integer> wPath = SkeletonTransform.getShapeContour(ws.toWormClusterSkeleton(),50);	
			//ArrayList<Vector2d> tpv = wpm.pixelListToVector2d(wPath);
			//ArrayList<Integer> rastShape = PolygonRasterizer.rasterize(ws.w,ws.h,tpv);
			//int[] wormDT = SkeletonUtils.listToMatrix(ws.w*ws.h, rastShape,ws.dt);			
			int[] thickness = getThickness(dtArray,ws, consecPts, numPoints);					
			this.thickness = thickness;	
		}
		
		/**
		 * Calculates the thickness associated to the consecutive skeleton points of
		 * the isolated worm given by skPoints.
		 * 
		 * @param wormDT Distance transformation of a single worm that has the size of the original 
		 * 	image
		 * @param wpm Worm image matcher
		 * @param skPoints Consecutive points belonging to the skeleton
		 * @param consecPts The points in the skeleton points array are
		 * 	consecutive
		 */
		public static int[] getThickness(int[] wormDT,WormSkeleton ws, 
				boolean consecPts, int numPoints){
				
				if(!consecPts){
					SkeletonUtils.makeConsecutive(ws);
				}				
				
				WormPixelMatcher wpm = ws.getPixelMatcher();
				//create skeleton spline  and take numPoints
				ArrayList<Point> base = wpm.baseToPoint(ws.getBasePoints());
				ArrayList<Point> points = wpm.pixelListToPoint(ws.getSkPoints());
				CardinalSpline skSpline = EvCardinalSpline.getShapeSpline(base,points,0.5,0.2);
				int[] profPts = wpm.pointListToPixel(EvCardinalSpline.getCardinalPoints(skSpline, numPoints));
				Line l1;
				ArrayList<Integer> contourPoints = new ArrayList<Integer>();
				double d1,d2;
				
				int[] thickness = new int[numPoints];

				//Calculate average distance to contour points and add to thickness
				for(int i=1;i<profPts.length -1;i++){								
					Vector2i[] extremes =bisectVector(wpm.getPixelPos(profPts[i-1]),wpm.getPixelPos(profPts[i]), wpm.getPixelPos(profPts[i+1]),wormDT[profPts[i]]);								
					l1 = new Line(wpm.getPixelPos(profPts[i]),extremes[0]);
					d1=distBestLinePoint(wormDT, wpm, l1, contourPoints);
					l1 = new Line(wpm.getPixelPos(profPts[i]),extremes[1]);
					d2=distBestLinePoint(wormDT, wpm, l1, contourPoints);									
					thickness[i]=(int)Math.ceil((d1+d2)/2);
				}				
				return thickness;
		}
		
		/**
		 * Calculates the worm shape main contour points over the given control points based on the
		 * calling object thickness. The size of controlPoints must be the same
		 * as the thickness variable, and must be ordered consecutively. 
		 */
		public static ArrayList<Integer> getContourPoints(int[] controlPoints,WormProfile wp){
			ArrayList<Integer> shapePoints = new ArrayList<Integer>(controlPoints.length*2);
			int[][] extremePixels = new int[controlPoints.length*2][2];
			
			//calculate extremes pixels
			for(int i=1;i<controlPoints.length -1;i++){											
				Vector2i[] extremes =bisectVector(wp.wpm.getPixelPos(controlPoints[i-1]),wp.wpm.getPixelPos(controlPoints[i]), wp.wpm.getPixelPos(controlPoints[i+1]),wp.thickness[i]);								
				System.out.print("CP1: "+wp.wpm.getPixelPos(controlPoints[i-1]));
				System.out.print(" CP2: "+wp.wpm.getPixelPos(controlPoints[i]));
				System.out.print(" CP3: "+wp.wpm.getPixelPos(controlPoints[i+1]));
				System.out.print(" THICK: "+wp.thickness[i]);
				System.out.println("Extremes " + extremes[0]+ " "+extremes[1]);
				
				extremePixels[i][0] = wp.wpm.posToPixel(extremes[0]);
				extremePixels[i][1] = wp.wpm.posToPixel(extremes[1]);
			}				
			
			//Add shape contour in counter-clockwise order
			shapePoints.add(controlPoints[0]);
			for(int i=1;i<controlPoints.length-1; i++){
				shapePoints.add(extremePixels[i][0]);
			}
			shapePoints.add(controlPoints[controlPoints.length-1]);
			for(int i=controlPoints.length-2;i>0; i--){
				shapePoints.add(extremePixels[i][1]);
			}				
			return shapePoints;
		}
		
		/**
		 * Calculates the worm shape contour points over the given control points based on the
		 * worm profile thickness. The size of controlPoints must be the same
		 * as the thickness variable, and must be ordered consecutively. 
		 */
		public static ArrayList<Integer> constructShape(int[] controlPoints, WormProfile wp,int numPoints){
			int[] baseA = {controlPoints[0],controlPoints[0]};
			ArrayList<Point> base = wp.wpm.baseToPoint(baseA);
			ArrayList<Integer> contourP = getContourPoints(controlPoints,wp);			
			return contourP;
			//CardinalSpline skSpline = EvCardinalSpline.getShapeSpline(base,wp.wpm.pixelListToPoint(contourP),0.5,1);
			//ArrayList<Integer> shapePoints = wp.wpm.pointListToPixelList(EvCardinalSpline.getCardinalPoints(skSpline, numPoints));
			//return shapePoints;
		}
		
		public static ArrayList getEx(int[] wormDT,WormSkeleton ws, 
				boolean consecPts, int numPoints){
				
				if(!consecPts){
					SkeletonUtils.makeConsecutive(ws);
				}				
				WormPixelMatcher wpm = ws.getPixelMatcher();
				//create skeleton spline  and take numPoints
				ArrayList<Point> base = wpm.baseToPoint(ws.getBasePoints());
				ArrayList<Point> points = wpm.pixelListToPoint(ws.getSkPoints());
				CardinalSpline skSpline = EvCardinalSpline.getShapeSpline(base,points,0.5,0.09);
				int[] profPts = wpm.pointListToPixel(EvCardinalSpline.getCardinalPoints(skSpline, numPoints));
				Line l1;
				ArrayList<Integer> contourPoints = new ArrayList<Integer>();
				double d1,d2;				
				int[] thickness = new int[numPoints];

				//Calculate average distance to contour points and add to thickness
				for(int i=1;i<profPts.length -1;i++){								
					Vector2i[] extremes =bisectVector(wpm.getPixelPos(profPts[i-1]),wpm.getPixelPos(profPts[i]), wpm.getPixelPos(profPts[i+1]),wormDT[profPts[i]]);								
					l1 = new Line(wpm.getPixelPos(profPts[i]),extremes[0]);
					d1=distBestLinePoint(wormDT, wpm, l1, contourPoints);
					l1 = new Line(wpm.getPixelPos(profPts[i]),extremes[1]);
					d2=distBestLinePoint(wormDT, wpm, l1, contourPoints);									
					thickness[i]=(int)Math.ceil((d1+d2)/2);
				}				
				return contourPoints;
		}
		
		
	private static double distBestLinePoint(int[] wormDT, WormPixelMatcher wpm, Line l1,
			ArrayList<Integer> contourPoints)
		{
		ArrayList<Integer> linePoints = l1.getLinePoints(wpm);
		Iterator<Integer> lit = linePoints.iterator();
		int bestPixel = -1;
		int bestDT =Integer.MAX_VALUE;
		int next;
		lit.next();//Avoid skPoint
		while (lit.hasNext())
			{
			next = lit.next();
			if (wormDT[next]!=0 && wormDT[next]<=bestDT)
				{
				bestPixel = next;
				bestDT = wormDT[next];
				contourPoints.add(next);
				}
			}
		// assign last pixel if not dt found (case for non-steep slopes)
		if (bestPixel==-1)
			{
			//Look for surrounding pixel, if not assign opposite
			bestPixel = linePoints.get(linePoints.size()-1);
			if(wormDT[bestPixel]==0) bestDT = Integer.MAX_VALUE;
			else bestDT = wormDT[bestPixel];
			}
		//Check if there is a better pixel around
		int[] neigh = SkeletonUtils.getCircularNeighbors(bestPixel, wpm.w);
		for(int i=0;i<neigh.length;i++){
			if(wormDT[neigh[i]]<bestDT && wormDT[neigh[i]]!=0){
				bestDT=wormDT[neigh[i]];
				bestPixel = neigh[i];
			}
		}
		contourPoints.add(bestPixel);
		Vector2i bp = wpm.getPixelPos(bestPixel);
		//Calculate distance 
		return Math.sqrt(Math.pow(bp.x-l1.p1.x,2)+Math.pow(bp.y-l1.p1.y,2));
		
		}
		
		/**
		 * Receives three image points, creates 2 vectors and calculates
		 * the resulting vector that bisects the angle between them. Returns the 
		 * the two opposite extreme point of the bisection vector that starts on p2 
		 */
		private static Vector2i[] bisectVector(Vector2i p1, Vector2i p2, Vector2i p3,int length){
			//Vector p2->p1
			ImVector2 v1 = new ImVector2((double)(p1.x-p2.x),(double)(p1.y-p2.y));
			//Vector p2->p3
			ImVector2 v2 = new ImVector2((double)(p3.x-p2.x),(double)(p3.y-p2.y));
			
			//Calculate bisection vector angle
			double acosParam = (v1.dot(v2))/(v1.length()*v2.length());
			if (acosParam<-1.0) acosParam = -1.0;
			else if(acosParam > 1.0) acosParam = 1.0;					
			double angle = Math.acos(acosParam);
			double degrees = Math.toDegrees(angle);
			degrees = degrees/2;
			
			//Calculate bisection vector
			angle = Math.toRadians(degrees);
			v1= v1.rotate(angle);
			v2 = v1.rotate(Math.toRadians(180));
			v1=v1.normalize().mul((double)length);
			v2=v2.normalize().mul((double)length);
			
			//Return the bisection extreme point translating to original
			Vector2i[] extremes = new Vector2i[2];
			extremes[0] = new Vector2i((int)Math.ceil(v1.x+p2.x),(int)Math.ceil(v1.y+p2.y));
			extremes[1] = new Vector2i((int)Math.ceil(v2.x+p2.x),(int)Math.ceil(v2.y+p2.y));
			
			return extremes;
		}
		
		public static ArrayList<Integer> calculateShapeContour(ArrayList<Integer> controlPoints){
			ArrayList<Integer> shape = new ArrayList<Integer>();							
			return shape;
		}
		
		private static WormSkeleton ensureCounterClockwise(WormSkeleton ws){
				int[] bases = ws.getBasePoints();
				
				
				return ws;
		}	
		
	}
