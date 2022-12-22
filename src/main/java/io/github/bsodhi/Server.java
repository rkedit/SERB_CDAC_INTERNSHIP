package io.github.bsodhi;

import in.datatype.PredResult;
import in.desco.tool.DescoTool;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;
import io.vertx.ext.web.templ.TemplateEngine;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * This class is the main entry point into this application.
 * @author Balwinder Sodhi
 */
public class Server extends AbstractVerticle {

    /**
     * Instance of the the server.
     */
    private HttpServer server;
    private static TemplateEngine engine = FreeMarkerTemplateEngine.create();
    private DescoTool desco;

    public Server(String backEndPath) throws IOException {
        desco = new DescoTool(backEndPath);
    }

    /**
     * Convenience method so you can run it in your IDE.
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java ");
        }
        try {
            Verticle myVerticle = new Server(args[0]);
            Vertx vertx = Vertx.vertx();
            vertx.deployVerticle(myVerticle);
            System.out.println("Deployed the verticle.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method starts the verticle.
     * @param startFuture
     */
    public void start(Future<Void> startFuture) {

        Router mainRouter = Router.router(vertx);
        Route baseRoute = mainRouter.route();
        // Serve static content from anywhere under "/"
        baseRoute.handler(StaticHandler.create());

        Router apiRtr = Router.router(vertx);
        Route allApi = apiRtr.route();
        /**
         * Handlers which are needed for all API requests. BodyHandler must be the first
         * one.
         */
        allApi.handler(BodyHandler.create());
        allApi.handler(CookieHandler.create());
        allApi.handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        // Mount the sub-souter
        mainRouter.mountSubRouter("/app", apiRtr);

        makeRoute(apiRtr, "/check").blockingHandler(ctx -> {
            int filesCount = ctx.fileUploads().size();
            if (filesCount < 1) {
                ctx.response().end("Not files received!");
                return;
            }
            File localFile = null;
            String lang = null;
            for (FileUpload item : ctx.fileUploads()) {
                String fn = item.fileName();
                localFile = new File(item.uploadedFileName());
                lang = fn.substring(fn.lastIndexOf(".")+1);
                long sz = item.size();
                ctx.put("FileName", fn);
                ctx.put("Size", sz);
                break;
            }
            try {
                Map<Integer, PredResult> pred = desco.performDefectEstimation(
                        localFile, lang);
                ctx.put("Pred", pred.values());
                ctx.put("Failed", false);
            } catch (IOException e) {
                ctx.put("Failed", true);
                System.out.println("Could not perform prediction. "+e.getLocalizedMessage());
            }
            // and now delegate to the engine to render it.
            engine.render(ctx, "templates", "/check.ftl", res->{
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                    System.out.println(">>> DONE with upload.");
                } else {
                    ctx.fail(res.cause());
                    System.out.println(">>> Failed to render template.");
                }
            });
        });

        server = vertx.createHttpServer().requestHandler(mainRouter::accept);

        // Now bind the server:
        server.listen(5500, res -> {
            if (res.succeeded()) {
                startFuture.complete();
                System.out.println("Started the server. Listening on 5500.");
            } else {
                startFuture.fail(res.cause());
            }
        });
    }

    private Route makeRoute(final Router restAPI, String path) {
        Route rt = restAPI.route(path);
        rt.produces("application/*").produces("text/*");
        return rt;
    }

}