package display;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Point;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class PlaneDrawer extends Applet {
	private TerrainMap terrainMap;
	private int width;
	private int height;

	private Point3d centerPoint;
	private SimpleUniverse universe;
	private TransformGroup objTrans;
	private Canvas3D canvas;
	private BranchGroup group;

	public PlaneDrawer(float[][] noiseMap) {
		terrainMap = new TerrainMap(noiseMap);
		height = noiseMap.length;
		width = noiseMap[0].length;
		setDefaults();
	}
	
	public PlaneDrawer(TerrainMap map){
		terrainMap = map;
		height = map.getHeight();
		width = map.getWidth();
		setDefaults();
	}
	
	private void setDefaults(){
		centerPoint = new Point3d();
		universe = null;
		canvas = null;
		group = null;
		objTrans = null;
	}

	public void setNoiseMaps(float[][] elevationMap, float[][] temperatureMap, float[][] moistureMap) {
		try {
			terrainMap = new TerrainMap(elevationMap, temperatureMap, moistureMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		height = terrainMap.getHeight();
		width = terrainMap.getWidth();
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

	public void init(boolean useWireframe, float amplify, float size) {
		removeAll();
		setLayout(new BorderLayout());

		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		canvas = new Canvas3D(config);
		universe = new SimpleUniverse(canvas);
		group = new BranchGroup();
		BranchGroup group2 = new BranchGroup();

		group.setCapability(Group.ALLOW_CHILDREN_READ);
		group.setCapability(Group.ALLOW_CHILDREN_WRITE);
		group.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		float scale = getScale(size); // The width and length

		add("Center", canvas);

		if (useWireframe) initWireframe(group2, amplify, scale);
		else initPlane(group2, amplify, scale);		

		DirectionalLight dl = getDirectionalLight(Color.white);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0);		
		Transform3D initialView = lookTowardsOriginFrom(new Point3d(0.0, 0.75, -(double)size * 2));
		objTrans = initMouseBehavior();

		dl.setInfluencingBounds(bounds);
		objTrans.addChild(group2);

		BranchGroup children = new BranchGroup();
		children.setCapability(BranchGroup.ALLOW_DETACH);
		children.addChild(objTrans);
		children.addChild(dl);

		group.addChild(children);

		universe.getViewingPlatform().getViewPlatformTransform().setTransform(initialView);
		universe.addBranchGraph(group);
	}

	public void initWireframe(BranchGroup group, float amplify, float scale) {
		LineStripArray[] mesh = getMesh(amplify, scale);
		float diff = -(float) centerPoint.y;

		for (int i = 0; i < mesh.length; ++i) {
			Shape3D meshLine = new Shape3D(mesh[i], createAppearance(true));

			TransformGroup centerPlane = new TransformGroup();
			Transform3D centerTrans = new Transform3D();
			Vector3f centerVect = new Vector3f(-(width / height) * (width / 8.0f), diff,
					-(height / width) * (height / 8.0f));

			centerTrans.setTranslation(centerVect);
			centerPlane.setTransform(centerTrans);
			centerPlane.addChild(meshLine);

			group.addChild(centerPlane);
		}
	}

	public void initPlane(BranchGroup group, float amplify, float scale){
		Shape3D[] quads = getQuads(amplify, scale);
		float diff = -(float) centerPoint.y;
		
		for (int i = 0; i < quads.length; ++i){
			TransformGroup centerPlane = new TransformGroup();
			Transform3D centerTrans = new Transform3D();
			Vector3f centerVect = new Vector3f(-(width / height) * (width / 8.0f), diff,
					-(height / width) * (height / 8.0f));
			
			centerTrans.setTranslation(centerVect);
			centerPlane.setTransform(centerTrans);
			centerPlane.addChild(quads[i]);
			
			group.addChild(centerPlane);
		}	
	}
	
	public void redraw(boolean useWireframe, float amplify, float size) {
		for (int i = 0; i < group.numChildren(); ++i){
			group.removeChild(group.getChild(i));
		}		

		BranchGroup group2 = new BranchGroup();
		
		if (useWireframe) initWireframe(group2, amplify, getScale(size));
		else initPlane(group2, amplify, getScale(size));

		TransformGroup objTrans = initMouseBehavior();
		
		objTrans.addChild(group2);

		BranchGroup children = new BranchGroup();
		children.setCapability(BranchGroup.ALLOW_DETACH);
		children.addChild(objTrans);
		children.addChild(getLight(Color.white));

		group.addChild(children);
	}
	
	private LineStripArray[] getMesh(float scaleHeight, float scaleLengthWidth) {
		LineStripArray[] mesh = new LineStripArray[width + height];

		// Row lines
		for (int row = 0; row < height; ++row) {
			Point3f[] rowVertices = new Point3f[width];

			for (int col = 0; col < width; ++col) {
				Point3f vert = new Point3f(terrainMap.getTerrainPoint(row, col).getPoint());
				alterPoint(vert, scaleHeight, scaleLengthWidth);
				rowVertices[col] = vert;

				if (row == height / 2 && col == width / 2) {
					centerPoint.x = vert.x;
					centerPoint.y = vert.y;
					centerPoint.z = vert.z;
				}
			}

			mesh[row] = new LineStripArray(width, GeometryArray.COORDINATES, new int[] { width });
			mesh[row].setCoordinates(0, rowVertices);
		}

		// Column lines
		for (int col = 0; col < width; ++col) {
			Point3f[] colVertices = new Point3f[height];

			for (int row = 0; row < height; ++row) {
				Point3f vert = new Point3f(terrainMap.getTerrainPoint(row, col).getPoint());
				alterPoint(vert, scaleHeight, scaleLengthWidth);
				colVertices[row] = vert;
			}

			mesh[height + col] = new LineStripArray(height, GeometryArray.COORDINATES, new int[] { height });
			mesh[height + col].setCoordinates(0, colVertices);
		}

		return mesh;
	}
	
	private Shape3D[] getQuads(float scaleHeight, float scaleLengthWidth){
		int quadCount = 0;
		Shape3D[] quads = new Shape3D[determineQuadCount()];
		
		for (int row = 0; row < height - 1; ++row){
			for (int col = 0; col < width - 1; ++col){
				QuadArray quadArray = new QuadArray(4, 
						GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR_3);
				
				int x1 = col;
				int x2 = col + 1;
				int y1 = row;
				int y2 = row + 1;
				
				Point3f[] points = { 
					new Point3f(terrainMap.getTerrainPoint(x1, y1).getPoint()),
					new Point3f(terrainMap.getTerrainPoint(x1, y2).getPoint()),
					new Point3f(terrainMap.getTerrainPoint(x2, y1).getPoint()),
					new Point3f(terrainMap.getTerrainPoint(x2, y2).getPoint())
				};
				
				Color[] colors = {
					terrainMap.getTerrainPoint(x1, y1).getBiome().color(), // bottom left
					terrainMap.getTerrainPoint(x1, y2).getBiome().color(), // bottom right
					terrainMap.getTerrainPoint(x2, y1).getBiome().color(), // top left
					terrainMap.getTerrainPoint(x2, y2).getBiome().color()  // top right
				};

				for (int i = 0; i < 4; ++i){
					alterPoint(points[i], scaleHeight, scaleLengthWidth);
					quadArray.setNormal(i, getNormal(points[i]));
					quadArray.setColor(i, new Color3f(colors[i]));
					quadArray.setCoordinate(i, points[i]);
				}	
				
				if (row == height / 2 && col == width / 2) {
					centerPoint.x = points[0].x;
					centerPoint.y = points[0].y;
					centerPoint.z = points[0].z;
				}
				
				Appearance ap = new Appearance();
				Material mat = new Material();
				Color3f specular = new Color3f(blendColors(colors));
				mat.setDiffuseColor(blendColors(colors));
				specular.scale(5.0f);
				specular.clampMax(0.8f);
				mat.setSpecularColor(specular);
				ap.setMaterial(mat);
				quads[quadCount++] = new Shape3D(quadArray, ap);
			}
		}
		
		return quads;		
	}
	
	private void alterPoint(Point3f point, float scaleHeight, float scaleLengthWidth){
		point.x *= scaleLengthWidth;
		point.y *= scaleHeight;
		point.z *= scaleLengthWidth;
	}
	
	private Vector3f getNormal(Point3f point){
		Vector3f normal = new Vector3f(point);
		normal.normalize();
		return normal;
	}
	
	private Color3f blendColors(Color[] colors){
		int r[] = new int[colors.length];
		int g[] = new int[colors.length];
		int b[] = new int[colors.length];
		
		for (int i = 0; i < colors.length; ++i){
			r[i] = colors[i].getRed();
			g[i] = colors[i].getGreen();
			b[i] = colors[i].getBlue();
		}
		
		int redSum	 = 0;
		int greenSum = 0;
		int blueSum  = 0;
		
		for (int i = 0; i < colors.length; ++i){
			redSum 	 += r[i];
			greenSum += g[i];
			blueSum  += b[i];
		}
		
		return new Color3f(
				redSum 	 / colors.length, 
				greenSum / colors.length,
				blueSum  / colors.length);
	}
	
	private TransformGroup initMouseBehavior(){
		if (objTrans != null){
			BranchGroup parent = (BranchGroup)objTrans.getParent();
			parent.removeChild(objTrans);
			objTrans.removeAllChildren();
		} 
		else {
			objTrans = new TransformGroup();
			objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			objTrans.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
			objTrans.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
			objTrans.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		}
		
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		
		MouseRotate objRotate = new MouseRotate();
		objRotate.setTransformGroup(objTrans);	
		objRotate.setSchedulingBounds(bounds);
		objRotate.setFactor(0.01, -0.01);
		
		MouseTranslate objTranslate = new MouseTranslate();
		objTranslate.setTransformGroup(objTrans);	
		objTranslate.setSchedulingBounds(bounds);
		objTranslate.setFactor(-0.01, 0.01);
		
		MouseWheelZoom objZoom = new MouseWheelZoom();
		objZoom.setTransformGroup(objTrans);
		objZoom.setSchedulingBounds(bounds);
		
		objTrans.addChild(objRotate);
		objTrans.addChild(objTranslate);
		objTrans.addChild(objZoom);
		
		
		return objTrans;
	}

	private float getScale(float size) {
		return size / ((width + height) / 2.0f);
	}

	private DirectionalLight getDirectionalLight(Color c) {
		return new DirectionalLight(new Color3f(c), new Vector3f(4.0f, -7.0f, -12.0f));
	}
	
	private AmbientLight getLight(Color c){
		return new AmbientLight(new Color3f(c));
	}

	private BranchGroup getAxisRef() {
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

	private Transform3D lookTowardsOriginFrom(Point3d point) {
		Transform3D move = new Transform3D();
		Vector3d up = new Vector3d(point.x, point.y + 1, point.z);
		move.lookAt(point, new Point3d(0.0d, 0.0d, 0.0d), up);

		return move;
	}

	private int determineQuadCount(){
		return (width - 1) * (height - 1);		
	}
}
