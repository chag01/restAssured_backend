import Tests.RequestValidation;
import Tests.Successful;
import Tests.TaskDcName010;


public class Main {

    public static void main(String[] args) throws InterruptedException {
        RequestValidation requestValidation = new RequestValidation();
        requestValidation.notSendRefContract();

        TaskDcName010 taskDcName010 = new TaskDcName010();
        taskDcName010.notExistsRefContract();
        taskDcName010.contractInStatusW();

        Successful successful = new Successful();
        successful.succesfulTest();
    }
}
