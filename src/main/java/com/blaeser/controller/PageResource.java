package com.blaeser.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("page")
public class PageResource {

	@GET
	public Response mainPage() {

		return Response.ok("This is the current main page !").build();
	}

	@Path("{pageName}")
	@GET
	public Response specialPage(@PathParam("pageName") String pageName) {

		return Response.ok("This is page " + pageName + " !").build();
	}
}
