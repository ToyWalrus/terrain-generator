package display;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;


public class PlaneDrawer extends Applet {
	private float[][] noiseMap;
	private Color[][] colorMap;
	private int width;
	private int height;
	private float maxY, minY;

	public PlaneDrawer(float[][] noiseMap, Color[][] colorMap) {
		this.colorMap = colorMap;
		this.noiseMap = noiseMap;
		height = noiseMap.length;
		width = noiseMap[0].length;
	}

	public static Appearance createAppearance(boolean wireframe) {
		Appearance ap = new Appearance();

		if (wireframe) {
			// Render as wireframe
			PolygonAttributes polyAttr = new PolygonAttributes();
			polyAttr.setPolygonMode(PolygonAttributes.POLYGON_LINE);
			polyAttr.setCullFace(PolygonAttributes.CULL_NONE);
			ap.setPolygonAttributes(polyAttr);
		} else {
			Material mat = new Material(new Color3f(Color.green), new Color3f(Color.black), new Color3f(Color.white),
					new Color3f(Color.black), 1.0f);
			ap.setMaterial(mat);
		}

		return ap;
	}

	public Vector3f normalize(Vector3f vect) {
		return normalize(vect.getX(), vect.getY(), vect.getZ());
	}

	public Vector3f normalize(double x, double y, double z) {
		double sum = x * x + y * y + z * z;
		if (Math.abs(sum - 1.0) > 0.001) {
			double root = Math.sqrt(sum) + 0.0000001;
			x /= root;
			y /= root;
			z /= root;
		}
		Vector3f normal = new Vector3f();
		normal.x = (float) x;
		normal.y = (float) y;
		normal.z = (float) z;

		return normal;
	}

	public LineStripArray getMap(float amplify) {
		amplify = amplify < 1.0f && amplify > -1.0f ? 1.0f : amplify;
		maxY = 0.0f;
		minY = amplify * 2;
		
		int numPoints = width * height;
		Point3f[] coords = new Point3f[numPoints];
		Color3f[] colors = new Color3f[numPoints];

		for (int row = 0; row < height; ++row) {
			for (int col = 0; col < width; ++col) {
				Point3f coord = new Point3f();
				Color c = colorMap[row][col];

				coord.x = col / 5.0f;
				coord.y = noiseMap[row][col] * amplify;
				coord.z = row / 5.0f;

				maxY = maxY > coord.y ? maxY : coord.y;
				minY = minY < coord.y ? minY : coord.y;
				
				coords[(row * width) + col] = coord;
				colors[(row * width) + col] = new Color3f(c);
			}
		}

		LineStripArray plane = new LineStripArray(numPoints,
				GeometryArray.COORDINATES | GeometryArray.COLOR_3 | GeometryArray.NORMALS,
				new int[] { numPoints });
		plane.setCoordinates(0, coords);
		plane.setColors(0, colors);
		
		return plane;
	}
	
	private LineStripArray[] getMesh(float scaleHeight, float scaleLengthWidth){
		LineStripArray[] mesh = new LineStripArray[width + height];
		
		// Row lines
		for (int row = 0; row < height; ++row){
			Point3f[] rowVertices = new Point3f[width];
			
			for (int col = 0; col < width; ++col){
				Point3f vert = new Point3f();
				
				vert.x = col * scaleLengthWidth;
				vert.y = noiseMap[row][col] * scaleHeight;
				vert.z = row * scaleLengthWidth;
				
				rowVertices[col] = vert;
			}
			
			mesh[row] = new LineStripArray(width,
					GeometryArray.COORDINATES,
					new int[] { width });
			mesh[row].setCoordinates(0, rowVertices);
		}
		
		// Column lines
		for (int col = 0; col < width; ++col){
			Point3f[] colVertices = new Point3f[height];
			
			for (int row = 0; row < height; ++row){
				Point3f vert = new Point3f();
				
				vert.x = col * scaleLengthWidth;
				vert.y = noiseMap[row][col] * scaleHeight;
				vert.z = row * scaleLengthWidth;
				
				colVertices[row] = vert;
			}
			
			mesh[height + col] = new LineStripArray(height,
					GeometryArray.COORDINATES,
					new int[] { height });
			mesh[height + col].setCoordinates(0, colVertices);
		}
		
		return mesh;
	}

	public void initPlane(boolean useWireframe){
		setLayout(new BorderLayout());
		
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		BranchGroup group = new BranchGroup();
		BranchGroup group2 = new BranchGroup();
		Canvas3D canvas = new Canvas3D(config);
		SimpleUniverse u = new SimpleUniverse(canvas);
		
		add("Center", canvas);
		group2 = getAxisPoint();

		float amplify = 25.0f;
		float scale = 0.5f;
		float RATIO = -0.7f;
		int row = 0;
		int col = 0;
		LineStripArray[] mesh = getMesh(amplify, scale);
		
		for (int i = 0; i < mesh.length; ++i){
			Shape3D meshLine = new Shape3D(mesh[i], createAppearance(useWireframe));
			
			TransformGroup centerPlane = new TransformGroup();
			Transform3D centerTrans = new Transform3D();
			Vector3f centerVect = new Vector3f(-width / height * (width / 4.0f), amplify * RATIO, -height / width * (height / 4.0f));
			
			centerTrans.setTranslation(centerVect);
			centerPlane.setTransform(centerTrans);
			centerPlane.addChild(meshLine);
			
			group2.addChild(centerPlane);			
		}		
		
		OrbitBehavior behavior = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ROTATE);
		DirectionalLight dl = new DirectionalLight(new Color3f(Color.cyan), new Vector3f(4.0f, -7.0f, -12.0f));
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0);
		TransformGroup objTrans = new TransformGroup();

		dl.setInfluencingBounds(bounds);
		objTrans.addChild(group2);
		
		behavior.setSchedulingBounds(bounds);
		behavior.setRotXFactor(1);
		behavior.setRotYFactor(1);
		
		group.addChild(dl);
		group.addChild(objTrans);

		u.getViewingPlatform().setViewPlatformBehavior(behavior);
		u.getViewingPlatform().setNominalViewingTransform();
		u.addBranchGraph(group);
	}
	
	private BranchGroup getAxisPoint(){
		BranchGroup group = new BranchGroup();
		for (float x = -1.0f; x < 1.0f; x += 0.1f) {
			Sphere s = new Sphere(0.05f);
			TransformGroup tg = new TransformGroup();
			Transform3D transform = new Transform3D();
			Vector3f vect = new Vector3f(x, 0.0f, 0.0f); // from -1.0 to 1.0 on
															// x axis

			transform.setTranslation(vect);
			tg.setTransform(transform);
			tg.addChild(s);
			group.addChild(tg);
		}

		for (float y = -1.0f; y < 1.0f; y += 0.1f) {
			Cone c = new Cone(0.05f, 0.1f);
			TransformGroup tg = new TransformGroup();
			Transform3D transform = new Transform3D();
			Vector3f vect = new Vector3f(0.0f, y, 0.0f); // from -1.0 to 1.0 on
															// y axis

			transform.setTranslation(vect);
			tg.setTransform(transform);
			tg.addChild(c);
			group.addChild(tg);
		}

		for (float z = -1.0f; z < 1.0f; z += 0.1f) {
			Cylinder c = new Cylinder(0.05f, 0.1f);
			TransformGroup tg = new TransformGroup();
			Transform3D transform = new Transform3D();
			Vector3f vect = new Vector3f(0.0f, 0.0f, z); // from -1.0 to 1.0 on
															// z axis

			transform.setTranslation(vect);
			tg.setTransform(transform);
			tg.addChild(c);
			group.addChild(tg);
		}
		
		return group;
	}
}
