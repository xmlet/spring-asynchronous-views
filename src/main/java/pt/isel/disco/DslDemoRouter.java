package pt.isel.disco;

import com.googlecode.jatl.HtmlWriter;
import htmlflow.HtmlFlow;
import j2html.rendering.IndentedHtml;
import j2html.tags.specialized.HtmlTag;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static j2html.TagCreator.*;

public class DslDemoRouter {
    public static RouterFunction<ServerResponse> dslDemoRouter() {
        return RouterFunctions
                .route()
                .path("/demo", builder -> builder
                        .GET("/jatl", req -> handlerJatl(req))
                        .GET("/j2html", req -> handlerJ2html(req))
                        .GET("/htmlflow", req -> handlerHtmlFlow(req))
                )
                .build();
    }

    private static Mono<ServerResponse> handlerJatl(ServerRequest req) {
        final var view = new HtmlWriter() {
            @Override
            protected void build() {
                html();
                  head();
                    title();
                      text("jatl");
                    end();
                  end();
                  body();
                    p();
                      text("From JATL");
                    end();
                  end();
                end();
            }
        };
        final var out = new AppendableWriter(writer -> {
            view.write(writer);
            writer.close();
            return null;
        });
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(out.asFlux(), new ParameterizedTypeReference<>() {
                });
    }

    private static Mono<ServerResponse> handlerJ2html(ServerRequest req) {
        final var view = html(
          head(
            title("j2html")
          ),
          body(
            p("From j2html")
          )
        );
        final var out = new AppendableWriter(writer -> {
            render(view, writer);
            writer.close();
            return null;
        });
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(out.asFlux(), new ParameterizedTypeReference<>() {
                });

    }

    private static void render(HtmlTag view, AppendableWriter writer) {
        try {
            view.render(IndentedHtml.into(writer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Mono<ServerResponse> handlerHtmlFlow(ServerRequest req) {
        final var view = HtmlFlow.view(page -> page
          .html()
            .head()
              .title()
                .text("HtmlFlow")
              .__()
            .__()
            .body()
              .p()
                .text("From HtmlFlow")
              .__()

            .__()
          .__()
        );
        final var out = new AppendableWriter(writer -> {
            view.setOut(writer).write();
            writer.close();
            return null;
        });
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(out.asFlux(), new ParameterizedTypeReference<>() {
                });

    }

    private static void sleep(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
