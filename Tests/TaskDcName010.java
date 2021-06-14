package Tests;

import Session.Session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import io.restassured.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;


public class TaskDcName010 {

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
    String prMess;
    String bpState;

    private Connection connection = null;
    private String dbUrl = "jdbc:oracle:thin:@SECURED";

    @Test
    @Order(1)
//Кейс: Передан отсутствующий в БД refContract .  Ожидаемый результат: сервис вернет ошибку с шага валидации TaskDCNAME010

    public void notExistsRefContract() throws InterruptedException {

        rReqMRef = generateReqMRef();
        rRefContract = "SAMDNXTESTX";

        int reqCounter = 0;
        int respCounter = 0;
        String req_dcName = "default";
        String resp_dcName = "default";

        Statement statement;
        ResultSet result;

        String dbRefContract = null;
        String lastValidationStep = null;
        String expectedLastValidationStep = "TaskDCNAME010";
        String SQL;

        Properties propsForP48deposit = new Properties();
        propsForP48deposit.setProperty("user", "SECURED");
        propsForP48deposit.setProperty("password", "SECURED");

        System.out.println("---------------------------Кейс: Передан отсутствующий в БД refContract .  Ожидаемый результат: сервис вернет ошибку с шага валидации TaskDCNAME010");
        System.out.println("---------------------------START: " + getClass().getName() + ";@Test(1) ---------------------------");
        System.out.println("\n---------------------------Step 1. Проверка отсутствия rRefContract в бд ---------------------------");

        try {
            System.out.println("---------------------------Устанавливаем соединение с базой " + dbUrl + " ---------------------------");
            connection = DriverManager.getConnection(dbUrl, propsForP48deposit);

            System.out.println("---------------------------Выполнение SQL скрипта... ---------------------------");
            SQL = "select refContract from catalog_dep48.wfDeposit where refContract='" + rRefContract + "'";

            System.out.println("---------------------------Выполнение скрипта окончено---------------------------" + "\n " + SQL);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            result = statement.executeQuery(SQL);

            while (result.next()) {
                dbRefContract = result.getString("refContract");
                System.out.println("Результат скрипта=" + dbRefContract);
            }

            connection.close();
            System.out.println("---------------------------Закрыто соединения с базой " + dbUrl + " ---------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        //проверка что в базе не найден переданный договор
        assertThat(dbRefContract, equalTo(null));

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
                prMess = response.path("'a.dcnameResponse'.prMess");

                if (prState.equalsIgnoreCase("e")
                        && prCode.equals("D00001")
                        && bpState.equalsIgnoreCase("e")
                        && prMess.equals("Contract [" + rRefContract + "] not found")) {
                    resp_dcName = "OK";
                    System.out.println("---------------------------Проверка ответа сервиса успешно пройдена " + getClass().getName() + ";Test(1)---------------------------");
                } else {
                    System.out.println("Ошибка при проверке ответа @Test(1) \n" +
                            "Счетчик запросов respCounter=" + respCounter + "\n" +
                            "Полученный prState=" + prState + "\n" +
                            "Полученный bpState=" + bpState + "\n" +
                            "Полученный prMess=" + prMess + "\n" +
                            "Полученный prCode=" + prCode);
                    respCounter++;
                    Thread.sleep(6000);
                }
            } else {
                System.out.println("---------------------------Ошибка @Test1. Счетчик проверки ответа достиг максимального значения. Завершение программы---------------------------");
                assertThat(resp_dcName, equalTo("OK"));
            }
        }

        System.out.println("\n---------------------------Step 4. Проверка шага валидации, на котором произошла ошибка ---------------------------");

        try {
            System.out.println("---------------------------Устанавливаем соединение с базой " + dbUrl + " ---------------------------");
            connection = DriverManager.getConnection(dbUrl, propsForP48deposit);

            System.out.println("---------------------------Выполняется SQL скрипт... ---------------------------");
            SQL = "select 'Валидация',act_id_,act_name_, start_time_, end_time_" +
                    " from wf_module_dep48_p.act_hi_actinst" +
                    " where proc_inst_id_ = " +
                    " (select CALL_PROC_INST_ID_ from wf_module_dep48_p.dcNameModel a join wf_module_dep48_p.act_hi_actinst b" +
                    " on procid = proc_inst_id_ where a.reqmref = '" + rReqMRef + "'" +
                    " and b.act_id_ = 'Validation')" +
                    " order by 4,5";

            System.out.println("---------------------------Выполнение скрипта окончено \n " + SQL);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            result = statement.executeQuery(SQL);

            //Проходимся по всем строкам и выводим значения столбца ACT_ID_
            while (result.next()) {
                System.out.println("Пройден шаг:" + result.getString("ACT_ID_"));
            }

            //Присваиваем резаулту последнюю строку и заполняем значением столбца ACT_ID_
            result.last();
            lastValidationStep = result.getString("ACT_ID_");

            connection.close();
            System.out.println("---------------------------Закрыто соединения с базой " + dbUrl + " ---------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        //проверка что последний шаг валидации тот который нам нужен
        assertThat(lastValidationStep, equalTo(expectedLastValidationStep));
        System.out.println("---------------------------Тест успешно пройден " + getClass().getName() + ";Test(1)---------------------------");
    }


    @Test
    @Order(2)

//Кейс: Передан депозитный договор в статусе 'w'.  Ожидаемый результат: сервис вернет ошибку с шага валидации TaskDCNAME010

    public void contractInStatusW() throws InterruptedException {

        int reqCounter = 0;
        int respCounter = 0;
        String req_dcName = "default";
        String resp_dcName = "default";

        Statement statement;
        ResultSet result;

        String dbRefContract = null;
        String lastValidationStep = null;
        String expectedLastValidationStep = "TaskDCNAME010";
        String SQL;

        Properties propsForP48deposit = new Properties();
        propsForP48deposit.setProperty("user", "SECURED");
        propsForP48deposit.setProperty("password", "SECURED");

        rRefContract = null;
        rReqMRef = generateReqMRef();

        System.out.println("---------------------------Кейс: Передан договор в статусе 'w'.  Ожидаемый результат: сервис вернет ошибку с шага валидации TaskDCNAME010");
        System.out.println("---------------------------START: " + getClass().getName() + ";@Test(2) ---------------------------");
        System.out.println("\n---------------------------Step 1. Получение договора в статусе 'w' ---------------------------");

        try {
            System.out.println("---------------------------Устанавливаем соединение с базой " + dbUrl + " ---------------------------");
            connection = DriverManager.getConnection(dbUrl, propsForP48deposit);

            System.out.println("---------------------------Выполнение SQL скрипта... ---------------------------");
            SQL = "select a.REFCONTRACT\n" +
                    "from wf_purse.WfPurseContract a\n" +
                    "join  catalog_dep48.wfDeposit b ON a.REFCONTRACT=b.REFCONTRACT\n" +
                    "where b.EKBID='1999999061'\n" +
                    "and a.PRODUCTTYPE ='DEP'\n" +
                    "and b.CREATELDAP='dn190192zdv'\n" +
                    "and a.ContractState = 'w'\n" +
                    "and rownum = 1";

            System.out.println("---------------------------Выполнение скрипта окончено---------------------------" + "\n " + SQL);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            result = statement.executeQuery(SQL);

            while (result.next()) {
                rRefContract = result.getString("REFCONTRACT");
                System.out.println("Результат скрипта=" + rRefContract);
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

        System.out.println("---------------------------Отправка запроса для вызова сервиса @Test(2) ---------------------------");

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
                    System.out.println("---------------------------Проверка запроса успешно пройдена " + getClass().getName() + ";Test(2)---------------------------");
                } else {
                    System.out.println("Ошибка при проверке @Test(2) \n" +
                            "Счетчик запросов reqCounter=" + reqCounter + "\n" +
                            "Полученный reqPrMess=" + reqPrMess + "\n" +
                            "Полученный reqPrState=" + reqPrState);
                    reqCounter++;
                    Thread.sleep(4000);
                }
            } else {
                System.out.println("---------------------------Ошибка @Test2. Счетчик проверки запроса достиг максимального значения. Завершение программы---------------------------");
                assertThat(req_dcName, equalTo("OK"));
            }
        }

        System.out.println("\n---------------------------Step 3. Получение и проверка ответа от сервиса ---------------------------");
        System.out.println("---------------------------Получение ответа @Test(2) ---------------------------");
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
                prMess = response.path("'a.dcnameResponse'.prMess");

                if (prState.equalsIgnoreCase("e")
                        && prCode.equals("D00015")
                        && bpState.equalsIgnoreCase("e")
                        && prMess.equals("Deposit [" + rRefContract + "] not found")) {
                    resp_dcName = "OK";
                    System.out.println("---------------------------Проверка ответа сервиса успешно пройдена " + getClass().getName() + ";Test(2)---------------------------");
                } else {
                    System.out.println("Ошибка при проверке ответа @Test(2) \n" +
                            "Счетчик запросов respCounter=" + respCounter + "\n" +
                            "Полученный prState=" + prState + "\n" +
                            "Полученный bpState=" + bpState + "\n" +
                            "Полученный prMess=" + prMess + "\n" +
                            "Полученный prCode=" + prCode);
                    respCounter++;
                    Thread.sleep(6000);
                }
            } else {
                System.out.println("---------------------------Ошибка @Test2. Счетчик проверки ответа достиг максимального значения. Завершение программы---------------------------");
                assertThat(resp_dcName, equalTo("OK"));
            }
        }

        System.out.println("\n---------------------------Step 4. Проверка шага валидации, на котором произошла ошибка ---------------------------");

        try {
            System.out.println("---------------------------Устанавливаем соединение с базой " + dbUrl + " ---------------------------");
            connection = DriverManager.getConnection(dbUrl, propsForP48deposit);

            System.out.println("---------------------------Выполняется SQL скрипт... ---------------------------");
            SQL = "select 'Валидация',act_id_,act_name_, start_time_, end_time_" +
                    " from wf_module_dep48_p.act_hi_actinst" +
                    " where proc_inst_id_ = " +
                    " (select CALL_PROC_INST_ID_ from wf_module_dep48_p.dcNameModel a join wf_module_dep48_p.act_hi_actinst b" +
                    " on procid = proc_inst_id_ where a.reqmref = '" + rReqMRef + "'" +
                    " and b.act_id_ = 'Validation')" +
                    " order by 4,5";

            System.out.println("---------------------------Выполнение скрипта окончено \n " + SQL);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            result = statement.executeQuery(SQL);

            //Проходимся по всем строкам и выводим значения столбца ACT_ID_
            while (result.next()) {
                System.out.println("Пройден шаг:" + result.getString("ACT_ID_"));
            }

            //Присваиваем резаулту последнюю строку и заполняем значением столбца ACT_ID_
            result.last();
            lastValidationStep = result.getString("ACT_ID_");

            connection.close();
            System.out.println("---------------------------Закрыто соединения с базой " + dbUrl + " ---------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        //проверка что последний шаг валидации тот который нам нужен
        assertThat(lastValidationStep, equalTo(expectedLastValidationStep));
        System.out.println("---------------------------Тест успешно пройден " + getClass().getName() + ";Test(2)---------------------------");
    }


    static private String generateReqMRef() {
        String reqMRef = RandomStringUtils.randomNumeric(15);
        return reqMRef;
    }
}
