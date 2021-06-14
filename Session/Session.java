package Session;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomUtils;
import org.hamcrest.Matchers;
import io.restassured.*;
import io.restassured.response.Response;
import io.restassured.path.xml.XmlPath;

import java.util.logging.Logger;


public class Session {

    public static void main(String[] args) {
    }

    public String getPoligonSession() {      // возвращает сессию
        final String uriPoligonSession = "SECURED";
        final String pathPoligonSession = "SECURED";
        final String ldapLogin = "SECURED";
        final String ldapPassword = "SECURED";

        String requestBody = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "  <session>\n" +
                "    <user auth=\"LDAP\" login=\"" + ldapLogin + "\" password=\"" + ldapPassword + "\"/>\n" +
                "  </session>";

        Response response = RestAssured.given()
                .baseUri(uriPoligonSession)
                .basePath(pathPoligonSession)
                .accept(ContentType.JSON)
                .header("Content-Type", "text/xml;charset=UTF-8")
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(200)
        .extract().response();

        String sid = response.jsonPath().get("value");
        System.out.println("---------------------------Response getSession:" +response.body().asString() + "---------------------------");

        if (!sid.isEmpty() && sid != null) {
            System.out.println("---------------------------Получена сессия:" + sid + "---------------------------");
        } else {
            System.out.println("---------------------------Сессия не получена---------------------------");
        }
        return sid;

    }

}