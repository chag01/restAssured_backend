package Tests;

import Session.Session;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import io.restassured.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;


public class RequestValidation {
    String baseUri = "SECURED";
    String requestPath = "SECURED";

    Session session = new Session();
    final String rSid = session.getPoligonSession();

    final String rReqMId = "TEST";
    String rReqMRef = RandomStringUtils.randomNumeric(15);

    final String rReqCUser = "SECURED";
    final String rReqBank = "PB";
    final String rChannel = "autoTest";
    String rContractName;

    String reqPrState;
    String reqPrMess;


    @Test
    @Order(1)
// Кейс: В запроcе не передан refContract.  Ожидаемый результат: реквест валидатор вернет ошибку

    public void notSendRefContract() throws InterruptedException {

        int reqCounter = 0;
        String req_dcName = "default";

        System.out.println("---------------------------Кейс: В запроcе не передан refContract.  Ожидаемый результат: реквест валидатор вернет ошибку");
        System.out.println("---------------------------START: " + getClass().getName() + ";@Test(1) ---------------------------");
        System.out.println("\n---------------------------Step 1. Вызов сервиса и проверка овтета ---------------------------");

        String requestBody = "{\"a.dcnameRequest\":{\n" +
                "\t\"reqMRef\":\"" + rReqMRef + "\",\n" +
                "\t\"reqMId\":\"" + rReqMId + "\",\n" +
                "\t\"reqCUser\":\"" + rReqCUser + "\",\n" +
                "\t\"reqBank\":\"" + rReqBank + "\",\n" +
                "\t\"channel\":\"" + rChannel + "\",\n" +
                "\t\"cbUrl\":\"autoTest\",\n" +
                "\t\"contractName\":\"" + rContractName + "\"}}";

        while (!req_dcName.equals("OK")) {
            if (reqCounter < 2) {

                Response requestResponse = RestAssured.given()
                        .contentType(ContentType.JSON)
                        .log().all()
                        .baseUri(baseUri)
                        .basePath(requestPath)
                        .accept(ContentType.JSON)
                        .header("wfsid", rSid)
                        .header("Content-Type", "application/json")
                        .body(requestBody)
                        .when()
                        .post()
                        .then()
                        .log().body()
                        .statusCode(200)
                        .extract().response();

                reqPrState = requestResponse.path("'a.dcnameResponse'.reqPrState"); // https://www.james-willett.com/rest-assured-gpath-json/
                reqPrMess = requestResponse.path("'a.dcnameResponse'.reqPrMess");

                if (reqPrState.equalsIgnoreCase("e")
                        && reqPrMess.equals("Business process CSRERR Error; ua.pb.p48.wf.module.dep48.exception.Dep48ModuleRequestException; [INVREQ] For request refContract can't be null; ")) {
                    req_dcName = "OK";
                    System.out.println("---------------------------Проверка запроса успешно пройдена " + getClass().getName() + ";Test(1)---------------------------");
                } else {
                    System.out.println("Ошибка при проверке @Test(1) \n" +
                            "Счетчик запросов reqCounter=" + reqCounter + "\n" +
                            "Полученный reqPrMess=" + reqPrMess + "\n" +
                            "Полученный reqPrState=" + reqPrState);
                    reqCounter++;
                    Thread.sleep(4000);
                }
            } else {
                System.out.println("---------------------------Ошибка @Test1. Счетчик проверки запроса достиг максимального значения. Завершение программы---------------------------");
                assertThat(req_dcName, equalTo("OK"));
            }
        }
        System.out.println("---------------------------Тест успешно пройден " + getClass().getName() + ";Test(1)---------------------------");
    }
}
