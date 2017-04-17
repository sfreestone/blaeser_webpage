package com.blaeser.services;

import com.blaeser.models.ContentImage;
import com.blaeser.models.ContentText;
import com.blaeser.models.Page;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PageService {

	public String createPage(String pageName) {

		StringBuffer sb = new StringBuffer();

		Page page = getPageData(pageName);

		if(page != null) {

			sb.append(page.getTemplate());
			sb.append("<br>");
			sb.append(page.getImages().get(0).getImageData());
			sb.append("<br>");
			sb.append(page.getImages().get(0).getDescription());
			sb.append("<br>");
			sb.append(page.getTexts().get(0).getContent());
			sb.append("<br>");
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
					"AND pc.type = 0 ");

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
					"AND pc.type = 1");
			
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
}
