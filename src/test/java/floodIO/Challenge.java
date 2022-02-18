package floodIO;

import java.util.*;
import java.util.function.Function;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;


public class Challenge extends Simulation {

    static final class Templates {

        public static final Function<Session, Map<String, Object>> templateMap = session -> {
            Map<String, Object> params = new HashMap<>();
            List<List<String>> parameters = session.getList("parameters");
            for (List<String> parameter : parameters) {
                params.put(parameter.get(0), parameter.get(1));
            }
            return params;
        };
    }

    {
        HttpProtocolBuilder httpProtocol = http
                .baseUrl("https://challenge.flood.io")
                .disableFollowRedirect()
                .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .acceptEncodingHeader("gzip, deflate, br")
                .acceptLanguageHeader("en-US,en;q=0.9")
                .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36")
                .inferHtmlResources(AllowList(), DenyList(".*\\.js", ".*\\.css", ".*css.*", ".*\\.gif", ".*\\.jpeg", ".*\\.jpg", ".*\\.ico", ".*\\.woff", ".*\\.woff2", ".*\\.(t|o)tf", ".*\\.png", ".*detectportal\\.firefox\\.com.*"));

        Map<CharSequence, String> headers_0 = new HashMap<>();
        headers_0.put("Sec-Fetch-Dest", "document");
        headers_0.put("Sec-Fetch-Mode", "navigate");
        headers_0.put("Sec-Fetch-Site", "none");
        headers_0.put("Sec-Fetch-User", "?1");
        headers_0.put("Upgrade-Insecure-Requests", "1");

        Map<CharSequence, String> headers_1 = new HashMap<>();
        headers_1.put("Origin", "https://challenge.flood.io");
        headers_1.put("Sec-Fetch-Dest", "document");
        headers_1.put("Sec-Fetch-Mode", "navigate");
        headers_1.put("Sec-Fetch-Site", "same-origin");
        headers_1.put("Sec-Fetch-User", "?1");
        headers_1.put("Upgrade-Insecure-Requests", "1");

        Map<CharSequence, String> headers_2 = new HashMap<>();
        headers_2.put("Accept", "*/*");
        headers_2.put("Sec-Fetch-Dest", "empty");
        headers_2.put("Sec-Fetch-Mode", "cors");
        headers_2.put("Sec-Fetch-Site", "same-origin");
        headers_2.put("X-Requested-With", "XMLHttpRequest");


        int thMin = 1;
        int thMax = 2;
        int testDuration = Integer.parseInt(System.getProperty("duration", "60"));
        int testUsers = Integer.parseInt(System.getProperty("users", "5"));

        ChainBuilder openHomePage =


                exec(
                        http("Get home page")
                                .get("/")
                                .headers(headers_0)
                                .check(css("input[name=authenticity_token]", "value").saveAs("authenticityToken"))
                                .check(css("#challenger_step_id", "value").saveAs("challengerStepId"))


                )
                        .pause(thMin, thMax);

        ChainBuilder startTheTest =
                exec(
                        http("Start The Test")
                                .post("/start")
                                .headers(headers_1)
                                .formParam("utf8", "✓")
                                .formParam("authenticity_token", "#{authenticityToken}")
                                .formParam("challenger[step_id]", "#{challengerStepId}")
                                .formParam("challenger[step_number]", "1")
                                .formParam("commit", "Start")
                                .check(status().is(302))
                )

                        .exec(
                                http("Get Age Page")
                                        .get("/step/2")
                                        .headers(headers_1)
                                        .check(css("#challenger_step_id", "value").saveAs("challengerStepId"))
                                        .check(regex("value=\"(\\d+)\">").findRandom().saveAs("selectedAge"))
                        )

                        .pause(thMin, thMax);

        ChainBuilder secondStep =
                exec(
                        http("Post Random Age")
                                .post("/start")
                                .headers(headers_1)
                                .formParam("utf8", "✓")
                                .formParam("authenticity_token", "#{authenticityToken}")
                                .formParam("challenger[step_id]", "#{challengerStepId}")
                                .formParam("challenger[step_number]", "2")
                                .formParam("challenger[age]", "#{selectedAge}") // use variable
                                .formParam("commit", "Next")
                                .check(status().is(302))
                )
                        .exec(
                                http("Get Largest Order Value Page")
                                        .get("/step/3")
                                        .headers(headers_1)
                                        .check(css("#challenger_step_id", "value").saveAs("challengerStepId"))
                                        .check(css("label[class='collection_radio_buttons']").findAll().transform(sl -> sl.stream().mapToInt(Integer::parseInt).max().orElse(0)).saveAs("orderValue"))
                                        .check(regex("value=\"(.*)\" /><label.*>#{orderValue}</label>").saveAs("orderSelected"))

                        )
                        .pause(thMin, thMax);

        ChainBuilder thirdStep =
                exec(
                        http("Post Largest Order Value")
                                .post("/start")
                                .headers(headers_1)
                                .formParam("utf8", "✓")
                                .formParam("authenticity_token", "#{authenticityToken}")
                                .formParam("challenger[step_id]", "#{challengerStepId}")
                                .formParam("challenger[step_number]", "3")
                                .formParam("challenger[largest_order]", "#{orderValue}")
                                .formParam("challenger[order_selected]", "#{orderSelected}")
                                .formParam("commit", "Next")
                                .check(status().is(302))
                )

                        .exec(
                                http("Get Simple Step Page")
                                        .get("/step/4")
                                        .headers(headers_1)
                                        .check(css("#challenger_step_id", "value").saveAs("challengerStepId"))
                                        .check(regex("<input id=\"challenger_order.* name=(\".*\") type.* value=(\"\\d+\") />").captureGroups(2).findAll().saveAs("parameters"))
                        )


                        .pause(thMin, thMax);

        ChainBuilder fourthStep =
                exec(

                        http("Click Next Button")
                                .post("/start")
                                .headers(headers_1)
                                .formParam("utf8", "✓")
                                .formParam("authenticity_token", "#{authenticityToken}")
                                .formParam("challenger[step_id]", "#{challengerStepId}")
                                .formParam("challenger[step_number]", "4")
                                .formParamMap(Templates.templateMap)
                                .formParam("commit", "Next")
                                .check(status().is(302))

                                .resources(
                                        http("Get One Time Token Page")
                                                .get("/step/5")
                                                .headers(headers_1)
                                                .check(css("#challenger_step_id", "value").saveAs("challengerStepId")),


                                        http("Get token Response")
                                                .get("/code")
                                                .headers(headers_1)
                                                .check(jsonPath("$.code").saveAs("token"))

                                )
                )
                        .pause(thMin, thMax);

        ChainBuilder fifthStep =
                exec(
                        http("Post One Time Token")
                                .post("/start")
                                .headers(headers_1)
                                .formParam("utf8", "✓")
                                .formParam("authenticity_token", "#{authenticityToken}")
                                .formParam("challenger[step_id]", "#{challengerStepId}")
                                .formParam("challenger[step_number]", "5")
                                .formParam("challenger[one_time_token]", "#{token}")
                                .formParam("commit", "Next")
                                .check(status().is(302))

                                .resources(
                                        http("Get Final Page")
                                                .get("/done")
                                                .headers(headers_2)
                                )
                );


        ScenarioBuilder scn = scenario("challenge.flood.io").exec(openHomePage, startTheTest, secondStep, thirdStep, fourthStep, fifthStep);


        setUp(scn.injectOpen(
            rampUsers(testUsers).during(testDuration)))
                .protocols(httpProtocol);
    }
}
