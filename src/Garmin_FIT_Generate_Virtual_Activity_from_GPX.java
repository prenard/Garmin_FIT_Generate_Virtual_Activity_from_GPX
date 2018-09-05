/*

https://openclassrooms.com/fr/courses/2654406-java-et-le-xml

*/

import java.io.File;
import java.io.IOException;

import java.lang.Math;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import javax.xml.xpath.XPathFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.garmin.fit.*;

public class Garmin_FIT_Generate_Virtual_Activity_from_GPX
{
	   public static void main(String[] args)
	   {
		      // Prints "Hello, World" in the terminal window.
		      System.out.println("Hello, World");
		      FileEncoder encode;
		      System.out.printf("FIT Encode Example Application - Protocol %d.%d Profile %.2f %s\n",
                        Fit.PROTOCOL_VERSION_MAJOR,
                        Fit.PROTOCOL_VERSION_MINOR,
                        Fit.PROFILE_VERSION / 100.0,
                        Fit.PROFILE_TYPE);
		      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		      try
		      {
		    	  	DocumentBuilder builder = factory.newDocumentBuilder();
		    	  	File fileXML = new File("Tour_Grand_Ballon_140_k.gpx");
		    	  	Document xml = builder.parse(fileXML);

		            Element root = xml.getDocumentElement();

		            XPathFactory xpf = XPathFactory.newInstance();
		            XPath path = xpf.newXPath();

		            Double current_latitude = (double) 0;
		            Double current_longitude = (double) 0;
		            Double current_elevation = (double) 0;
		            Double current_grade = (double) 0;

		            Double previous_latitude = (double) 0;
		            Double previous_longitude = (double) 0;
		            Double previous_elevation = (double) 0;

		            Double delta_elevation = (double) 0;
		            Double delta_distance_horizontal = (double) 0;
		            Double delta_distance = (double) 0;

		            Double total_distance = (double) 0;
		            
		            String expression = "//trkpt";
		            NodeList list = (NodeList)path.evaluate(expression, root, XPathConstants.NODESET);
		            int nodesLength = list.getLength();

		            for(int i = 0 ; i < nodesLength; i++)
		            {
	            		Element trkpt_Element = (Element) list.item(i);

	            		current_latitude = Double.parseDouble(trkpt_Element.getAttribute("lat"));
	            		current_longitude = Double.parseDouble(trkpt_Element.getAttribute("lon"));
		            	/*current_elevation = (Double)path.evaluate("//trkpt/ele[1]", root, XPathConstants.NUMBER);*/

			            XPathFactory xpf_ele = XPathFactory.newInstance();
			            XPath path_ele = xpf_ele.newXPath();
			            String expression_ele = "./ele";
			            current_elevation = (Double)path.evaluate(expression_ele, trkpt_Element, XPathConstants.NUMBER);

		            	
		            	if (i >0)
		            	{
		            		delta_distance_horizontal = 6378137 * Math.acos(Math.sin(deg2rad(previous_latitude)) * Math.sin(deg2rad(current_latitude)) + Math.cos(deg2rad(previous_latitude)) * Math.cos(deg2rad(current_latitude)) * Math.cos(deg2rad(current_longitude) - deg2rad(previous_longitude)));
		            		delta_elevation = current_elevation - previous_elevation;
		            		delta_distance = Math.sqrt(Math.pow(delta_distance_horizontal, 2) + Math.pow(current_elevation - previous_elevation,2));
		            		total_distance += delta_distance;
		            		current_grade = delta_elevation / delta_distance;
		            	}

		            	System.out.println("trkseg # " + i + " : ");

		            	System.out.println("current_latitude = " + current_latitude);
		            	System.out.println("current_longitude = " + current_longitude);
		            	System.out.println("current_elevation = " + current_elevation);

		            	System.out.println("delta_elevation = " + delta_elevation);
		            	System.out.println("delta_distance = " + delta_distance);
		            	System.out.println("current_grade = " + current_grade);

		            	previous_latitude = current_latitude;
		            	previous_longitude = current_longitude;
		            	previous_elevation = current_elevation;
		            }
		            System.out.println("Total Distance = " + total_distance);
		      }
		      catch (ParserConfigurationException e)
		      {
		          e.printStackTrace();
		      }
		      catch (SAXException e)
		      {
		          e.printStackTrace();
		      }
		      catch (IOException e)
		      {
		          e.printStackTrace();
		      }
		      catch (XPathExpressionException e)
		      {
		          e.printStackTrace();
		       }
	   }

	   private static double deg2rad(double deg)
	   {
			return (deg * Math.PI / 180.0);
	   }
}