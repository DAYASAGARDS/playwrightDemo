import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PlaywrightFirst {

    private static String userName;
    private static String password;

    public static void readFromProperties() {

        Properties properties = new Properties();
        FileInputStream inputStream = null;

        try {

            // Get the current working directory of the Java process
            String projectPath = System.getProperty("user.dir");

            // Construct the file path relative to the project root directory
            String filePath = projectPath + "/src/main/resources/config.properties";

            inputStream = new FileInputStream(filePath);
            properties.load(inputStream);

            // Read property values
            userName = properties.getProperty("imedidata.username");
            password = properties.getProperty("imedidata.password");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void upload(Browser browser){
        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        page.navigate("https://davidwalsh.name/demo/multiple-file-upload.php");

        // upload file
        page.setInputFiles("input#filesToUpload", Paths.get("uploadfile1.txt"));
        Locator locator = page.locator("//ul[@id='fileList']/li");
        locator.allTextContents().forEach(element -> System.out.println("1"+element));
        //upload multiple files
        page.setInputFiles("input#filesToUpload", new Path[]{Paths.get("uploadfile1.txt"), Paths.get("uploadfile2.txt")});
        locator = page.locator("//ul[@id='fileList']/li");
        locator.allInnerTexts().forEach(element -> System.out.println("2"+element));// --------different way to find elements
        //remove all the file
        page.setInputFiles("input#filesToUpload", new Path[0]);
        locator = page.locator("//ul[@id='fileList']/li");
        locator.allTextContents().forEach(element -> System.out.println("3"+element));
        //Create a runtime file and upload
        page.setInputFiles("input#filesToUpload", new FilePayload("uploadfileGenerate.txt","text/plain", "this is daya".getBytes(StandardCharsets.UTF_8)));
        locator = page.locator("//ul[@id='fileList']/li");
        locator.allInnerTexts().stream().forEach(element -> System.out.println("4"+element));// --------different way to find elements

    }

    public void download(Browser browser){
        //BrowserContext googleContext = browser.newContext();
        BrowserContext downloadContext = browser.newContext();
        //Page googlePage = googleContext.newPage();
        //googlePage.navigate("https://www.google.com");

        Page downloadPage = downloadContext.newPage();

        //Download file
        downloadPage.navigate("https://chromedriver.storage.googleapis.com/index.html?path=111.0.5563.41/");
        Download download = downloadPage.waitForDownload(() ->{
            downloadPage.click("a:text('chromedriver_mac64.zip')");
        });
        //download.cancel();
        //download.failure();
        download.saveAs(Paths.get("chromedriver"));
        System.out.println(download.url() +"\n" +download.page().title() + "\n"
                + download.suggestedFilename()  // updated file name
                + download.path()); // original file name
    }

    public void maximise(Browser browser){

        //THis tool kit helps you to get the screensize
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)screensize.getWidth();
        int height = (int)screensize.getHeight();
        //pass that height and width
        BrowserContext maximise = browser.newContext(new Browser.NewContextOptions().setViewportSize(width,height));

        //add screensize
        //BrowserContext maximise = browser.newContext(new Browser.NewContextOptions().setViewportSize(1920,1080));
        Page maximisePage = maximise.newPage();


        maximisePage.navigate("https://sandbox.imedidata.net");
        System.out.println(String.format( "width =  %s  height =  %s ",width,height));
    }

    public void recording(Browser browser){
        //Set the path the name
        BrowserContext context = browser.newContext(new Browser.NewContextOptions().setRecordVideoDir(Paths.get("recordedVideo/"))//remeber this is project path
                //.setRecordVideoSize(1920,1080) if we want specific size
        );

        Page page = context.newPage();
        page.navigate("https://sandbox.imedidata.net");
        //commented becasue using the automatic login of playright
        page.getByTestId("username").click();
        page.getByTestId("username").fill(userName);
        page.getByTestId("username").press("Tab");
        page.getByTestId("password").fill(password);
        page.getByTestId("sign_in_button").click();
        page.locator("//*[@id='app_type_1']/div[4]/a").click();


        //Save the login json for first time for imedidata
        //context.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get("imedidataSandboxLogin.json")));

        page.getByPlaceholder("Search for Study").click();
        page.getByPlaceholder("Search for Study").fill("R2M");
        page.getByPlaceholder("Search for Study").press("Enter");

        //make sure you close the page and context otherwise the video will not record
        page.close();
        context.close();


    }

    public void imedidata(Browser browser){
        //if i have to use login everything
        // BrowserContext context = browser.newContext();

        //becasue i altready logedin and using brower context
        BrowserContext context = browser.newContext(new Browser.NewContextOptions().setStorageStatePath(Paths.get("imedidataSandboxLogin.json")));



        // Start tracing before creating / navigating a page.
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));


        Page page = context.newPage();

        page.navigate("https://sandbox.imedidata.net");
        //commented becasue using the automatic login of playright
        page.getByTestId("username").click();
        page.getByTestId("username").fill(userName);
        page.getByTestId("username").press("Tab");
        page.getByTestId("password").fill(password);
        page.getByTestId("sign_in_button").click();
        page.locator("//*[@id='app_type_1']/div[4]/a").click();


        //Save the login json for first time for imedidata
        //context.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get("imedidataSandboxLogin.json")));

        page.getByPlaceholder("Search for Study").click();
        page.getByPlaceholder("Search for Study").fill("R2M");
        page.getByPlaceholder("Search for Study").press("Enter");



        // Stop tracing and export it into a zip archive.
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace.zip")));
    }

    public void traceViewerRecord(Browser browser){
        //if i have to use login everything
        BrowserContext context = browser.newContext();

        // Start tracing before creating / navigating a page.
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));


        Page page = context.newPage();

        page.navigate("https://sandbox.imedidata.net");
        //commented becasue using the automatic login of playright
        page.getByTestId("username").click();
        page.getByTestId("username").fill(userName);
        page.getByTestId("username").press("Tab");
        page.getByTestId("password").fill(password);
        page.getByTestId("sign_in_button").click();
        page.locator("//*[@id='app_type_1']/div[4]/a").click();


        page.getByPlaceholder("Search for Study").click();
        page.getByPlaceholder("Search for Study").fill("R2M");
        page.getByPlaceholder("Search for Study").press("Enter");
        // if you put in run config it will pause PWDEBUG=1
        //page.pause()

        // Stop tracing and export it into a zip archive.
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace.zip")));

        // use this command to see what is the record trace look like
        //mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="show-trace trace.zip"
    }

    public void automaticLogin(Browser browser){
        //BrowserContext context = browser.newContext();

        //becasue i altready logedin and using brower context
        BrowserContext context = browser.newContext(new Browser.NewContextOptions().setStorageStatePath(Paths.get("imedidataSandboxLogin.json")));

        Page page = context.newPage();

        page.navigate("https://sandbox.imedidata.net");
        //commented becasue using the automatic login of playright
/*                    page.getByTestId("username").click();
                    page.getByTestId("username").fill(userName);
                    page.getByTestId("username").press("Tab");
                    page.getByTestId("password").fill(password);
                    page.getByTestId("sign_in_button").click();*/
                    page.locator("//*[@id='app_type_1']/div[4]/a").click();


        //Save the login json for first time for imedidata
        //context.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get("imedidataSandboxLogin.json")));

        page.getByPlaceholder("Search for Study").click();
        page.getByPlaceholder("Search for Study").fill("R2M");
        page.getByPlaceholder("Search for Study").press("Enter");
    }

    public void windowsPopup(Browser browser){

        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        page.navigate("https://sandbox.imedidata.net");

        Page popup = page.waitForPopup(() -> {
            page.click("a:text('Help documentation')");
        });
        popup.waitForLoadState(); // this will wait until popup is fully loaded
        System.out.println("popup title = " + popup.title());
        System.out.println("main page = " + page.title());
    }

    public void javascriptAlertsandWebPopups(Browser browser) {

        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        page.navigate("https://the-internet.herokuapp.com/javascript_alerts");

        //Automatic Aleart by playright it clicks ok
        page.click("//button[text()='Click for JS Alert']");
        String automaticAleart = page.textContent("#result");

        System.out.println("1 Automatic Aleart = " + automaticAleart); // if you want to get to know what it is then you need to use below method


        //Confirm like accept or Cancel. // once you write here, it will use this for this page everywhere.
        page.onDialog(dialog -> {
            String text = dialog.message();
            System.out.println("2 Confirm Text box = "+text);
            dialog.accept("Daya here");
            //dialog.dismiss();
        });
        page.click("//button[text()='Click for JS Confirm']");
        String confirmAlert = page.textContent("#result");

        System.out.println("3 Confirm Aleart = " + confirmAlert);


        page.click("//button[text()='Click for JS Prompt']");
        String commentAleart = page.textContent("#result");

        System.out.println("5 Comment Aleart = " + commentAleart);

    }

    public static void main(String[] args) {

        PlaywrightFirst playwrightFirst = new PlaywrightFirst();

        readFromProperties();

                try (Playwright playwright = Playwright.create()) {
                    Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                            .setHeadless(false));
                    playwrightFirst.automaticLogin(browser);
                    browser.close();

        }
    }
}
