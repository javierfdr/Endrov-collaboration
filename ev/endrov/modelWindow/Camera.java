/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindow;

import javax.vecmath.*;
import javax.media.opengl.*;

import org.jdom.Element;

import endrov.util.EvXmlUtil;

//Vector3D vs javax.vecmath.....? part of javax.media.*

public class Camera
	{
	/** Camera position */
	public Vector3d pos=new Vector3d(0.0,2.0,-15.0);

	/** Center position */
	public Vector3d center=new Vector3d(0,0,0);
	
	/** Transformation matrix */
	private final Matrix3d mat=new Matrix3d();
	
	
	
	public Camera()
		{
		mat.setIdentity();
		}
	
	public Camera(Camera cam)
		{
		pos.set(cam.pos);
		center.set(cam.center);
		mat.set(cam.mat);
		}
	
	public void toElement(Element e)
		{
		Element epos=new Element("pos");
		Element ecenter=new Element("center");
		Element emat=new Element("mat");
		EvXmlUtil.vector2element(epos, pos);
		EvXmlUtil.vector2element(ecenter, center);
		EvXmlUtil.matrix2element(emat, mat);
		e.addContent(epos);
		e.addContent(ecenter);
		e.addContent(emat);
		}
	
	public void fromElement(Element e)
		{
		Element epos=e.getChild("pos");
		Element ecenter=e.getChild("center");
		Element emat=e.getChild("mat");
		EvXmlUtil.element2vector(epos, pos);
		EvXmlUtil.element2vector(ecenter, center);
		EvXmlUtil.element2matrix(emat, mat);
		}
	
	public Vector3d transformedVector(double x, double y, double z)
		{
		Vector3d v=new Vector3d();
		Vector3d vfront=new Vector3d(0,0,z);
		Vector3d vup=new Vector3d(0,y,0);
		Vector3d vright=new Vector3d(x,0,0);
		mat.transform(vfront);
		mat.transform(vup);
		mat.transform(vright);
		v.add(vfront);
		v.add(vup);
		v.add(vright);
		return v;
		}
	
	/**
	 * Move camera relative to camera coordinate system
	 */
	public void moveCamera(double x, double y, double z)
		{
		pos.add(transformedVector(x, y, z));
		/*
		Vector3d vfront=new Vector3d(0,0,z);
		Vector3d vup=new Vector3d(0,y,0);
		Vector3d vright=new Vector3d(x,0,0);
		mat.transform(vfront);
		mat.transform(vup);
		mat.transform(vright);
		pos.add(vfront);
		pos.add(vup);
		pos.add(vright);*/
		}
	
	/**
	 * Rotate around camera, relative to camera
	 */
	public void rotateCamera(double x, double y, double z)
		{
		Matrix3d matx2=new Matrix3d();	matx2.rotX(-x); //rotation camera up/down
		Matrix3d maty2=new Matrix3d();	maty2.rotY(-y); //rotation camera left/right
		Matrix3d matz2=new Matrix3d();	matz2.rotZ(-z); //rotation around camera axis
		mat.mul(matz2);
		mat.mul(maty2);
		mat.mul(matx2);
		}
	
	
	/**
	 * Set the rotation of the camera
	 */
	public void setRotation(double x, double y, double z)
		{
		mat.setIdentity();
		rotateCamera(x,y,z);
		}

	/**
	 * Set rotation of camera through matrix
	 */
	public void setRotationMatrix(Matrix3d mat)
		{
		this.mat.set(mat);
		}

	/**
	 * Get the rotation matrix. May not be written
	 */
	public Matrix3d getRotationMatrixReadOnly()
		{
		return mat;
		}
	
	/**
	 * Do the GL transformation to move into camera coordinates
	 */
	public void transformGL(GL gl)
		{
		mulMatGL(gl, mat);
		gl.glTranslated(-pos.x, -pos.y, -pos.z);
		}
	
	/**
	 * Inverse GL camera rotation
	 */
	public void unrotateGL(GL gl)
		{
		Matrix3d inv=new Matrix3d();
		inv.invert(mat);
		mulMatGL(gl, inv);
		}

	/**
	 * Put a java media matrix on the GL stack
	 */
	private static void mulMatGL(GL gl, Matrix3d mat)
		{
		gl.glMultMatrixd(new double[]{
				mat.m00, mat.m01, mat.m02,0,
				mat.m10, mat.m11, mat.m12,0,
				mat.m20, mat.m21, mat.m22,0,
				0,       0,       0,      1},0);
		}
	
	/**
	 * Transform a point world coord to cam coord
	 */
	private Vector3d transformPoint(Vector3d v)
		{
		Matrix3d inv=new Matrix3d();	inv.invert(mat);
		Vector3d u=new Vector3d(v);		u.sub(pos);
		inv.transform(u);
		return u;
		}

	/**
	 * Rotate camera around center
	 */
	public void rotateCenter(double x, double y, double z)
		{
		Vector3d cameraCenter=transformPoint(center);
		rotateCamera(x, y, z);
		Vector3d cameraCenterNew=transformPoint(center);
		Vector3d diff=new Vector3d();
		diff.sub(cameraCenterNew,cameraCenter);
		moveCamera(diff.x, diff.y, diff.z);
		}
	
	/**
	 * Make camera move at center at some distance with current rotation
	 */
	public void center(double dist)
		{
		Vector3d frontv=new Vector3d(0,0,dist);
		mat.transform(frontv);
		pos.add(frontv, center);
		}
	}
