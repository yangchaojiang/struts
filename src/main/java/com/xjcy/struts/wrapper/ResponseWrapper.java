package com.xjcy.struts.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.xjcy.struts.context.WebContextUtils;
import com.xjcy.struts.mapper.JSONObj;
import com.xjcy.struts.mapper.ModelAndView;

public class ResponseWrapper
{
	private static final Logger logger = Logger.getLogger(ResponseWrapper.class);

	private static final String CONTENT_ENCODING = "UTF-8";
	private final JspWrapper jspWrapper = new JspWrapper();
	private Class<?> returnType;
	private Object resultObj;

	public void setReturnObj(Class<?> returnType, Object resultObj)
	{
		this.returnType = returnType;
		this.resultObj = resultObj;
	}

	public void doResponse(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		if (returnType.equals(Void.class) || returnType.equals(void.class))
			dealNone(resultObj, request, response);
		else if (returnType.equals(ModelAndView.class))
			dealView(resultObj, request, response);
		else if (returnType.equals(String.class))
			dealString(resultObj, request, response);
		else if (returnType.equals(JSONObj.class))
			dealJSON((JSONObj) resultObj, request, response);
		else
			throw new ServletException("不支持的返回类型 " + returnType.getName());
	}

	private void dealNone(Object resultObj, HttpServletRequest request, HttpServletResponse response)
	{
		if (logger.isDebugEnabled())
			logger.debug("自定义Response");
	}

	private void dealView(Object resultObj, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		ModelAndView mav = (ModelAndView) resultObj;
		mav.fillRequest(request);
		jspWrapper.processJsp(mav.getViewName(), request, response);
		if (logger.isDebugEnabled())
			logger.debug("Forward to " + mav.getViewName());
	}

	private void dealString(Object resultObj, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String text = resultObj.toString();
		response.setContentType(WebContextUtils.CONTENT_TYPE_TEXT);
		writeResponse(response, request, text);
		if (logger.isDebugEnabled())
			logger.debug("[TEXT]" + text);
	}

	private void dealJSON(JSONObj obj, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String json = obj.toString();
		response.setContentType(WebContextUtils.CONTENT_UTF8_JSON);
		writeResponse(response, request, json);
		if (logger.isDebugEnabled())
			logger.debug("[JSON]" + json);
	}

	public static void writeResponse(HttpServletResponse response, HttpServletRequest request, String text)
			throws IOException, ServletException
	{
		int len = text.length();
		if (WebContextUtils.isGZipEncoding(request) && len > 256)
		{
			byte[] data = WebContextUtils.text2byte(text, CONTENT_ENCODING);
			if (data == null)
				throw new ServletException("Response data can not be null");
			writeGZipData(response, data, len);
		}
		else
		{
			PrintWriter out = response.getWriter();
			out.print(text);
			out.close();
		}
	}

	private static void writeGZipData(HttpServletResponse response, byte[] data, int jsonLen) throws IOException
	{
		long start = System.nanoTime();
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		GZIPOutputStream output = new GZIPOutputStream(byteOutput);
		output.write(data);
		output.close();
		byte[] gzipData = byteOutput.toByteArray();
		byteOutput.close();
		if (logger.isDebugEnabled())
			logger.debug("Compress gzip from " + jsonLen + " to " + gzipData.length + " in "
					+ (System.nanoTime() - start) + " ns");
		response.addHeader("Content-Encoding", "gzip");
		response.setContentLength(gzipData.length);
		ServletOutputStream output2 = response.getOutputStream();
		output2.write(gzipData);
		output2.flush();
		output2.close();
	}
}
