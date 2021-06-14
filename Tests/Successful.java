package Tests;

import Session.Session;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;


public class Successful {

    final String baseUri = "SECURED";
    final String requestPath = "SECURED";
    final String responsePath = "SECURED";

    Session session = new Session();
    final String rSid = session.getPoligonSession();

    final String rReqMId = "TEST";
    String rReqMRef = null;

    final String rReqCUser = "SECURED";
    final String rReqBank = "PB";
    final String rChannel = "autoTest";
    String rRefContract;
    String rqContractName = "testContractName";

    String reqPrState;
    String reqPrCode;
    String reqPrMess;
    String prState;
    String prCode;
    String bpState;

    private Connection connection = null;
    private String dbUrl = "SECURED";

    @Test
    @Order(1)
//Кейс: Корректный тест.  Ожидаемый результат: сервис полностью отработает. Наименование депозита изменено

    public void succesfulTest() throws InterruptedException {

        rReqMRef = generateReqMRef();
        rqContractName = "autoTestName_DCNAME" + RandomStringUtils.randomNumeric(9);

        int reqCounter = 0;
        int respCounter = 0;
        String req_dcName = "default";
        String resp_dcName = "default";

        Statement statement;
        ResultSet result;

        String expectedLastValidationStep = "TaskDCNAME010";
        String dbPurseId = null;
        String dbRefContract = null;
        String dbContractName = null;
        String SQL;

        Properties propsForP48deposit = new Properties();
        propsForP48deposit.setProperty("user", "SECURED");
        propsForP48deposit.setProperty("password", "SECURED");

        rRefContract = null;
        rReqMRef = generateReqMRef();

        System.out.println("---------------------------Кейс: Корректный тест.  Ожидаемый результат: сервис полностью отработает. Наименование депозита изменено");
        System.out.println("---------------------------START: " + getClass().getName() + ";@Test(1) ---------------------------");
        System.out.println("\n---------------------------Step 1. Получение данных о договоре ---------------------------");

        try {
            System.out.println("---------------------------Устанавливаем соединение с базой " + dbUrl + " ---------------------------");
            connection = DriverManager.getConnection(dbUrl, propsForP48deposit);

            System.out.println("---------------------------Выполнение SQL скрипта... ---------------------------");
            SQL = "select a.REFCONTRACT, b.version, a.PURSEID\n" +
                    "from wf_purse.WfPurseContract a\n" +
                    "join  catalog_dep48.wfDeposit b ON a.REFCONTRACT=b.REFCONTRACT\n" +
                    "where b.EKBID='1999999061'\n" +
                    "and a.PRODUCTTYPE ='DEP'\n" +
                    "and b.CREATELDAP='dn190192zdv'\n" +
                    "and a.ContractState !='w'\n" +
                    "and rownum = 1\n";

            System.out.println("---------------------------Выполнение скрипта окончено---------------------------" + "\n " + SQL);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            result = statement.executeQuery(SQL);

            while (result.next()) {
                rRefContract = result.getString("REFCONTRACT");
                dbPurseId = result.getString("PURSEID");
                System.out.println("Результат скрипта:" +
                        "\nrefContract=" + rRefContract +
                        "\npurseId=" + dbPurseId);
            }

            connection.close();
            System.out.println("---------------------------Закрыто соединения с базой " + dbUrl + " ---------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        //проверка что в базе найден договор
        assertThat(rRefContract, is(not(nullValue())));

        System.out.println("\n---------------------------Step 2. Вызов сервиса и проверка ответа ---------------------------");

        String requestBody = "{\"a.dcnameRequest\":{\n" +
                "\t\"reqMRef\":\"" + rReqMRef + "\",\n" +
                "\t\"reqMId\":\"" + rReqMId + "\",\n" +
                "\t\"reqCUser\":\"" + rReqCUser + "\",\n" +
                "\t\"reqBank\":\"" + rReqBank + "\",\n" +
                "\t\"channel\":\"" + rChannel + "\",\n" +
                "\t\"cbUrl\":\"autoTest\",\n" +
                "\t\"refContract\":\"" + rRefContract + "\",\n" +
                "\t\"contractName\":\"" + rqContractName + "\"}}";

        System.out.println("---------------------------Отправка запроса для вызова сервиса @Test(1) ---------------------------");

        while (!req_dcName.equals("OK")) {
            if (reqCounter < 3) {

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

                reqPrState = requestResponse.path("'a.dcnameResponse'.reqPrState");
                reqPrCode = requestResponse.path("'a.dcnameResponse'.reqPrCode");

                if (reqPrState.equalsIgnoreCase("r")
                        && reqPrCode.equals("000000")) {
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

        System.out.println("\n---------------------------Step 3. Получение и проверка ответа от сервиса ---------------------------");
        System.out.println("---------------------------Получение ответа @Test(1) ---------------------------");
        Thread.sleep(10000);

        String responseBody = "{\"a.dcnameRequest\":{\n" +
                "\t\"reqMRef\":\"" + rReqMRef + "\",\n" +
                "\t\"reqMId\":\"" + rReqMId + "\",\n" +
                "\t\"reqBank\":\"" + rReqBank + "\"}}";

        while (!resp_dcName.equals("OK")) {
            if (respCounter < 3) {

                Response response = RestAssured.given()
                        .contentType(ContentType.JSON)
                        .log().all()
                        .baseUri(baseUri)
                        .basePath(responsePath)
                        .accept(ContentType.JSON)
                        .header("wfsid", rSid)
                        .header("Content-Type", "application/json")
                        .body(responseBody)
                        .when()
                        .post()
                        .then()
                        .log().body()
                        .statusCode(200)
                        .extract().response();

                prState = response.path("'a.dcnameResponse'.prState");
                bpState = response.path("'a.dcnameResponse'.bpState");
                prCode = response.path("'a.dcnameResponse'.prCode");

                if (prState.equalsIgnoreCase("r")
                        && prCode.equals("000000")
                        && bpState.equalsIgnoreCase("r")) {
                    resp_dcName = "OK";
                    System.out.println("---------------------------Проверка ответа сервиса успешно пройдена " + getClass().getName() + ";Test(1)---------------------------");
                } else {
                    System.out.println("Ошибка при проверке ответа @Test(1) \n" +
                            "Счетчик запросов respCounter=" + respCounter + "\n" +
                            "Полученный prState=" + prState + "\n" +
                            "Полученный bpState=" + bpState + "\n" +
                            "Полученный prCode=" + prCode);
                    respCounter++;
                    Thread.sleep(6000);
                }
            } else {
                System.out.println("---------------------------Ошибка @Test1. Счетчик проверки ответа достиг максимального значения. Завершение программы---------------------------");
                assertThat(resp_dcName, equalTo("OK"));
            }
        }

        System.out.println("\n---------------------------Step 4. Проверка создяния модели ---------------------------");

        try {
            System.out.println("---------------------------Устанавливаем соединение с базой " + dbUrl + " ---------------------------");
            connection = DriverManager.getConnection(dbUrl, propsForP48deposit);

            System.out.println("---------------------------Выполняется SQL скрипт... ---------------------------");
            SQL = "select REFCONTRACT \n" +
                    "from WF_MODULE_DEP48_p.DCNAMEMODEL\n" +
                    "WHERE ID is not null\n" +
                    "and BPSIMPLENAME = 'DCNAME'\n" +
                    "and BPSTATE = 'r'\n" +
                    "and CHANNEL = '" + rChannel + "'\n" +
                    "and PRCODE = '000000'\n" +
                    "and PRDATE >= to_date (sysdate, 'dd.mm.yyyy') \n" +
                    "and PRID = '1'\n" +
                    "and PRMESS is null\n" +
                    "and PRSTATE = 'r'\n" +
                    "and PROCID is not null\n" +
                    "and REQBANK = '" + rReqBank + "'\n" +
                    "and REQCDATE >= to_date (sysdate, 'dd.mm.yyyy') \n" +
                    "and REQCUSER = '" + rReqCUser + "'\n" +
                    "and REQMID = '" + rReqMId + "'\n" +
                    "and REQMREF = '" + rReqMRef + "'\n" +
                    "and CONTRACTNAME = '" + rqContractName + "'\n" +
                    "and REFCONTRACT = '" + rRefContract + "'";

            System.out.println("---------------------------Выполнение скрипта окончено \n " + SQL);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            result = statement.executeQuery(SQL);

            while (result.next()) {
                dbRefContract = result.getString("REFCONTRACT");
            }

            System.out.println("\nРезультат скрипта:" +
                    "\nrefContract=" + dbRefContract);

            assertThat(dbRefContract, equalTo(rRefContract));

            connection.close();
            System.out.println("---------------------------Закрыто соединения с базой " + dbUrl + " ---------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }


        System.out.println("\n---------------------------Step 5. Проверка изменения наименования депозита в catalog_dep48.wfDeposit . Task020 ---------------------------");

        try {
            System.out.println("---------------------------Устанавливаем соединение с базой " + dbUrl + " ---------------------------");
            connection = DriverManager.getConnection(dbUrl, propsForP48deposit);

            System.out.println("---------------------------Выполняется SQL скрипт... ---------------------------");
            SQL = "select REFCONTRACT, CONTRACTNAME\n" +
                    "from catalog_dep48.wfDeposit\n" +
                    "where REFCONTRACT = '" + rRefContract + "'\n" +
                    "and CONTRACTNAME = '" + rqContractName + "'\n" +
                    "and UPDATEDATE like to_date (sysdate, 'dd.mm.yyyy') ";

            System.out.println("---------------------------Выполнение скрипта окончено \n " + SQL);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            result = statement.executeQuery(SQL);

            while (result.next()) {
                dbRefContract = result.getString("REFCONTRACT");
                dbContractName = result.getString("CONTRACTNAME");
            }

            System.out.println("Результат скрипта:" +
                    "\nrefContract=" + dbRefContract +
                    "\ncontractName=" + dbContractName);

            assertThat(dbRefContract, equalTo(rRefContract));
            assertThat(dbContractName, equalTo(rqContractName));

            connection.close();
            System.out.println("---------------------------Закрыто соединения с базой " + dbUrl + " ---------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("---------------------------Тест успешно пройден " + getClass().getName() + ";Test(1)---------------------------");
    }



    static private String generateReqMRef() {
        String reqMRef = RandomStringUtils.randomNumeric(15);
        return reqMRef;
    }
}

