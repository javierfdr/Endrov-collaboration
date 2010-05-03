package endrov.worms;

import endrov.util.Vector2i;
import endrov.util.curves.EvCardinalSpline;
import endrov.worms.skeleton.WormSkeleton;

import java.util.ArrayList;
import java.util.Iterator;
import com.graphbuilder.curve.Point;

public class WormDescriptor{
	
	WormPixelMatcher pixelMatcher;
	EvCardinalSpline cs;
	
	public WormDescriptor(WormPixelMatcher wm){
			this.pixelMatcher = wm;
	}
	
		public static void getShapeSpline(WormSkeleton ws, double alpha){

		}
	
		public Point pixelToPoint(int pixel){		
			Vector2i pixel2D = pixelMatcher.getPixelPos(pixel);
			return (new PointFactory()).createPoint(pixel2D.x,pixel2D.y);
		}
		
		
		/**
		 * Returns a list of Point transforming each integer point
		 * in the given integer list points
		 * 
		 * @param points List of integer points
		 */
		public ArrayList<Point> pixelListToPoint(ArrayList<Integer> points){
			ArrayList<Point> pointList = new ArrayList<Point>(points.size());
			Iterator<Integer> pIt = points.iterator();
			while(pIt.hasNext()){
				pointList.add(pixelToPoint(pIt.next()));
			}			
			return pointList;
		}
		
		/**
		 * Returns an array of int transforming each Point from points
		 * list to the corresponding integer matrix value
		 * @param points
		 * @return
		 */
		
		public int[] pointListToPixel(ArrayList<Point> points){
			int[] pixels = new int[points.size()];		
			Iterator<Point> it = points.iterator();
			Point p;
			int count=0;
			while(it.hasNext()){
				pixels[count] = pixelMatcher.pointToPixel(it.next());
				count++;				       	
			}
			return pixels;
		}
		
		public ArrayList<Point> baseToPoint(int[] basePoints){
			if (basePoints.length !=2) return null;
			ArrayList<Point> pl = new ArrayList<Point>(2);
			pl.add(pixelToPoint(basePoints[0]));
			pl.add(pixelToPoint(basePoints[1]));
			
			return pl;
		}
		
	}
