/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;


import com.sun.opengl.util.BufferUtil;

//www.typhoonlabs.com/tutorials/glsl/Chapter_5.pdf

/**
 * GLSL shader
 * 
 * @author Johan Henriksson
 */
public class Shader
	{
	private Integer idf;
	private Integer idv;
	private int prog;
	
	private String uploadURL(GL gl, int thisid, URL src, String type) throws IOException
		{
		BufferedReader brf = new BufferedReader(new InputStreamReader(src.openStream()));
		String fsrc = "";
		String line;
		while ((line=brf.readLine()) != null)
		  fsrc += line + "\n";
		gl.glShaderSource(thisid, 1, new String[]{fsrc}, new int[]{fsrc.length()}, 0);
		gl.glCompileShader(thisid);
	  checkLogInfo(gl,thisid,type);
		return fsrc;
		}
	
	/**
	 * Create a shader. srcv and srcf can be null.
	 * use .class.getResource(...) to obtain URL.
	 */
	public Shader(GL gl, URL srcv, URL srcf)
		{
		try
			{
			ModelView.checkerr(gl);
			if(srcv!=null)
				{
				idv=gl.glCreateShader(GL.GL_VERTEX_SHADER);
				uploadURL(gl, idv, srcv,"v");
				}
			if(srcf!=null)
				{
				idf=gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
				uploadURL(gl, idf, srcf,"f");
				}
			
			ModelView.checkerr(gl);
			prog = gl.glCreateProgram();
			ModelView.checkerr(gl);
			if(idv!=null)	gl.glAttachShader(prog, idv);
			if(idf!=null)	gl.glAttachShader(prog, idf);
			ModelView.checkerr(gl);
			gl.glLinkProgram(prog);
			ModelView.checkerr(gl);
			gl.glValidateProgram(prog);
			ModelView.checkerr(gl);
			System.out.println("prog "+prog+" "+idv+" "+idf);
			}
		catch (IOException e)
			{
			System.out.println("Could read sources "+srcv+" "+srcf);
			e.printStackTrace();
			}
		}
	/*
	private void checkerr(GL gl, String pos)
		{
		int errcode=gl.glGetError();
		if(errcode!=GL.GL_NO_ERROR)
			{
			try
				{
				throw new Exception("GL error: "+new GLU().gluErrorString(errcode));
				}
			catch (Exception e)
				{
				//System.out.println("GL error: "+new GLU().gluErrorString(errcode));
				e.printStackTrace();
				}
//			System.out.println("error ("+pos+") "+new GLU().gluErrorString(errcode));
			}
		}
	*/
	
  private void checkLogInfo(GL gl, int obj, String type)
  	{
  	IntBuffer iVal = BufferUtil.newIntBuffer(1);
  	gl.glGetObjectParameterivARB(obj, GL.GL_OBJECT_INFO_LOG_LENGTH_ARB, iVal);
  	int length = iVal.get();

  	if (length > 2) 
  		{
  		ByteBuffer infoLog = BufferUtil.newByteBuffer(length);

  		iVal.flip();
  		gl.glGetInfoLogARB(obj, length, iVal, infoLog);

  		byte[] infoBytes = new byte[length];
  		infoLog.get(infoBytes);
  		System.out.println("GLSL Validation ("+type+") >> " + new String(infoBytes));
  		}
  	}

	
	public void use(GL gl)
		{
//		int scaleLoc=gl.glGetUniformLocation(prog, "scale");
//		gl.glUniform1f(scaleLoc, 1);


//		gl.glEnable(GL.GL_VERTEX_PROGRAM_ARB);
		ModelView.checkerr(gl);
//		gl.glEnable(GL.GL_FRAGMENT_PROGRAM_ARB);
		ModelView.checkerr(gl);
		gl.glUseProgram(prog);
		ModelView.checkerr(gl);
		
		//before bind
		int texUnit=0;
//		gl.glActiveTexture(GL.GL_TEXTURE0 + texUnit);
		
		
		ModelView.checkerr(gl);
		
		int texLoc=gl.glGetUniformLocation(prog, "tex");
		ModelView.checkerr(gl);
		gl.glUniform1i(texLoc, texUnit);

		
		ModelView.checkerr(gl);

		
		}
	
	public void stopUse(GL gl)
		{
//		gl.glDisable(GL.GL_VERTEX_PROGRAM_ARB);
		ModelView.checkerr(gl);
//		gl.glDisable(GL.GL_FRAGMENT_PROGRAM_ARB);
		ModelView.checkerr(gl);
		gl.glUseProgram(0);
		ModelView.checkerr(gl);
		}
	
	public void delete(GL gl)
		{
		if(idv!=null)	{gl.glDetachShader(prog, idv); gl.glDeleteShader(idv);}
		if(idf!=null)	{gl.glDetachShader(prog, idf); gl.glDeleteShader(idf);}
		gl.glDeleteProgram(prog);
		}
	
	}
