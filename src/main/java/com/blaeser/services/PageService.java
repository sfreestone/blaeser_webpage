package com.blaeser.services;

import com.blaeser.models.ContentImage;
import com.blaeser.models.ContentText;
import com.blaeser.models.Page;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.swing.text.html.parser.Parser;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageService {

	public String createPage(String pageName) {

		StringBuffer sb = new StringBuffer();

		Page page = getPageData(pageName);

		if(page != null) {

//			sb.append(page.getTemplate());
//			sb.append("<br>");
//			sb.append(page.getImages().get(0).getImageData());
//			sb.append("<br>");
//			sb.append(page.getImages().get(0).getDescription());
//			sb.append("<br>");
//			sb.append(page.getTexts().get(0).getContent());
//			sb.append("<br>");

			sb.append(insertContent(page.getTemplate(), page.getImages(), page.getTexts()));
		}

		return sb.toString();
	}

	private Page getPageData(String pageName) {

		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		Page page = null;

		try {
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/TestDB");

			// This works too
			// Context envCtx = (Context) ctx.lookup("java:comp/env");
			// DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");

			conn = ds.getConnection();
			st = conn.createStatement();
			page = new Page();

			rs = st.executeQuery("" +
					"SELECT * " +
					"FROM page AS p " +
					"INNER JOIN page_template AS pt " +
					"ON p.templateId = pt.id " +
					"WHERE p.name = '" + pageName + "' " +
					"AND p.active = 1 ");

			while (rs.next()) {

				page.setId(rs.getInt("id"));
				page.setName(rs.getString("name"));
				page.setActive(rs.getBoolean("active"));
				page.setTemplateId(rs.getInt("templateId"));
				page.setCreationDate(rs.getDate("creationDate"));
				page.setModifyDate(rs.getDate("modifyDate"));
				page.setTemplate(rs.getString("template"));
			}

			rs = st.executeQuery("" +
					"SELECT * " +
					"FROM page_content AS pc " +
					"INNER JOIN image AS i " +
					"ON pc.refid = i.id " +
					"WHERE pc.pageId = " + page.getId() + " " +
					"AND pc.type = 1 ");

			List images = new ArrayList<ContentImage>();

			while (rs.next()) {

				ContentImage image = new ContentImage();
				image.setId(rs.getInt("id"));
				image.setImageData(rs.getString("imageData"));
				image.setWidth(rs.getInt("width"));
				image.setHeight(rs.getInt("height"));
				image.setDescription(rs.getString("description"));

				images.add(image);
			}

			page.setImages(images);

			rs = st.executeQuery("SELECT * " +
					"FROM page_content AS pc " +
					"INNER JOIN text AS t " +
					"ON pc.refid = t.id " +
					"WHERE pc.pageId = " + page.getId() + " " +
					"AND pc.type = 0");
			
			List texts = new ArrayList<ContentText>();

			while (rs.next()) {

				ContentText text = new ContentText();
				text.setId(rs.getInt("id"));
				text.setContent(rs.getString("content"));

				texts.add(text);
			}

			page.setTexts(texts);

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			try { if (st != null) st.close(); } catch (SQLException e) { e.printStackTrace(); }
			try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
		}

		return page;
	}

	private String insertContent(String template, List<ContentImage> images, List<ContentText> texts) {

		StringBuffer sb = new StringBuffer();
		StringBuffer sbResult = new StringBuffer();

		sb.append(template);

		// add more patterns if needed (each pattern needs one numeric group (back reference))
		List<Pattern> patterns = new ArrayList<Pattern>();
		patterns.add(Pattern.compile("\\{image([0-9])+}"));
//		TODO patterns.add(Pattern.compile("\\{text([0-9])+}"));

		for(Pattern p : patterns) {

			Matcher matcher = p.matcher(sb);

			while(matcher.find()) {

				try {
					// TODO sbResult as temporary stringbuffer needed ?
					final int index = Integer.parseInt(matcher.group(1));
					matcher.appendReplacement(sbResult, images.get(index - 1).getImageData());	// TODO
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			matcher.appendTail(sbResult);

			// TODO check this (copy back to sb)
			sb.setLength(0);
			sb.append(sbResult);
		}

		return sb.toString();
	}
}
