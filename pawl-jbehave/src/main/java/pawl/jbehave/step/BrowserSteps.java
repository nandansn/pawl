/*
 * Copyright 2015 Geeoz Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pawl.jbehave.step;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import pawl.jbehave.Pages;
import pawl.util.Resources;
import pawl.util.WebExpectedConditions;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * <code>BrowserSteps</code> a simple POJO, which will contain the Java methods
 * that are mapped to the textual steps. The methods need to annotated with
 * one of the JBehave annotations and the annotated value should contain
 * a regex pattern that matches the textual step.
 *
 * @author Alex Voloshyn
 * @author Mike Dolinin
 * @author Serge Voloshyn
 * @version 1.16 5/2/15
 */
public final class BrowserSteps extends Matchers {
    /**
     * Default logger.
     */
    private static final Logger LOG =
            Logger.getLogger(BrowserSteps.class.getName());
    /**
     * Web pages collection factory.
     */
    private final transient Pages browser;
    /**
     * Specifies the URL of the page.
     */
    private transient String url;

    /**
     * Create steps object that contains pages collection.
     *
     * @param pages factory of the page handlers
     */
    public BrowserSteps(final Pages pages) {
        super();
        browser = pages;
    }

    /**
     * Setup URL for next user actions.
     *
     * @param key for resources to get corresponding value
     */
    @Given("an '$key' link")
    public void setupLink(final String key) {
        if (Resources.context().containsKey(key)) {
            url = Resources.context().get(key);
        } else {
            url = Resources.base().string(key);
        }
    }

    /**
     * Action for open web link.
     */
    @When("I open the link")
    public void openUrl() {
        browser.base().get(url);
    }

    /**
     * Action for refresh web page.
     */
    @When("I refresh the page")
    @Alias("refresh the page")
    public void refreshPage() {
        browser.base().navigate().refresh();
    }

    /**
     * Action for open new web link by adding to opened link context path.
     *
     * @param contextPath path for add
     */
    @When("open context path '$contextPath'")
    public void openContextPath(final String contextPath) {
        String currentUrl = browser.base().getCurrentUrl();
        browser.base().get(currentUrl + contextPath);
    }

    /**
     * Action for click web element.
     *
     * @param identity element identity for search
     */
    @When("I click '$identity'")
    @Alias("click '$identity'")
    public void click(final String identity) {
        browser.base().find(identity).click();
    }

    /**
     * Action for click the web element with XY offsets from center of element.
     *
     * @param identity element identity for search
     * @param xOffset  X-offset for click
     * @param yOffset  Y-offset for click
     */
    @When("I click '$identity' with '$xOffset' and '$yOffset' offsets")
    @Alias("click '$identity' with '$xOffset' and '$yOffset' offsets")
    public void clickWithOffset(final String identity,
                                final String xOffset,
                                final String yOffset) {
        final WebElement webElement = browser.base().find(identity);
        final Actions builder = new Actions(browser.base());
        builder.moveToElement(webElement).moveByOffset(
                Integer.parseInt(xOffset), Integer.parseInt(yOffset))
                // change after
                // https://code.google.com/p/selenium/issues/detail?id=6141
                // fixed
                .click().perform();
    }

    /**
     * Action for click web element.
     *
     * @param className        element className for search
     * @param value            element text
     * @param parentIdentifier parent element identifier
     */
    @When("I choose '$className' with '$value' in '$parentIdentifier'")
    @Alias("choose '$className' with '$value' in '$parentIdentifier'")
    public void choose(final String className, final String value,
                       final String parentIdentifier) {
        final WebElement parent = browser.base().find(parentIdentifier);
        final List<WebElement> elements
                = parent.findElements(By.className(className));
        assertThat("Page elements should exists: class name '" + className
                        + "'", elements.size(),
                is(not(equalTo(0))));

        for (final WebElement element : elements) {
            if (element.getText().equals(value)) {
                element.click();
                return;
            }
        }
        fail("Cannot find element with class name '" + className + "' and "
                + "value '" + value + "'.");
    }

    /**
     * Fill input with local file absolute path.
     *
     * @param identity         input element id
     * @param fileRelativePath relative file path
     */
    @When("I fill '$identity' with '$fileRelativePath' file")
    @Alias("fill '$identity' with '$fileRelativePath' file")
    public void fillFile(final String identity, final String fileRelativePath) {
        final URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(fileRelativePath);
        assert resource != null;
        final WebElement element = browser.base().find(identity);

        if (element.isDisplayed()) {
            element.clear();
            element.sendKeys(resource.getFile());
            return;
        }

        final String putValue = MessageFormat.format(
                "document.getElementById(\"{0}\")"
                        + ".setAttribute(\"value\", \"{1}\");"
                        + "if (\"createEvent\" in document) '{'"
                        + "var evt = document.createEvent(\"HTMLEvents\");"
                        + "evt.initEvent(\"change\", false, true);"
                        + "document.getElementById(\"{0}\").dispatchEvent(evt);"
                        + "'}' else '{'document.getElementById(\"{0}\"')'"
                        + ".fireEvent(\"onchange\");'}'",
                identity, resource.getFile());
        LOG.warning("Put new value script: " + putValue);
        browser.base().executeScript(putValue);
    }

    /**
     * Action for click on link.
     *
     * @param href attribute for search link on page
     */
    @When("I click on link '$href'")
    public void clickOnLinkWithAttribute(final String href) {
        browser.base().findElement(
                By.xpath(".//a[@href='" + href + "']")).click();
    }

    /**
     * Action for filling text to input element.
     *
     * @param identity element identity for search
     * @param text     text that should be filled by step
     */
    @When("I fill '$identity' with '$value'")
    @Alias("fill '$identity' with '$value'")
    public void fill(final String identity, final String text) {
        String textFromStorageIfExist = getTextFromStorageIfExist(
                Resources.base().string(text, text));
        browser.base().find(identity).fillWith(textFromStorageIfExist);
    }

    /**
     * Action for press ENTER key.
     *
     * @param identity element identity for search
     */
    @When("I press ENTER on '$identity'")
    @Alias("press ENTER on '$identity'")
    public void pressEnter(final String identity) {
        browser.base().find(identity).sendKeys(Keys.ENTER);
    }

    /**
     * Action for selection option in the select element.
     *
     * @param identity element identity for search
     * @param value    value that should be selected
     */
    @When("I select '$identity' with '$value'")
    @Alias("select '$identity' with '$value'")
    public void select(final String identity, final String value) {
        final WebElement element = browser.base().find(identity);
        final Select select = new Select(element);
        select.selectByVisibleText(Resources.base().string(value, value));
    }

    /**
     * Retrieve text from test session store.
     *
     * @param text text key that used for storing
     * @return a text from test session store
     */
    private String getTextFromStorageIfExist(final String text) {
        final String storedText = Resources.context().get(text);
        if (storedText != null) {
            return storedText;
        }
        return text;
    }

    /**
     * Verify the current page title.
     *
     * @param title for check
     */
    @Then("I get title '$title'")
    public void verifyTitle(final String title) {
        browser.base().getWait().until(ExpectedConditions
                .titleIs(Resources.base().string(title, title)));
    }

    /**
     * Search text on the current page.
     *
     * @param text for search
     */
    @Then("I get text '$text'")
    @Alias("text '$text'")
    public void verifySource(final String text) {
        browser.base().getWait().until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.tagName("body"),
                        Resources.base().string(text, text)));
    }

    /**
     * Search HTML element on the current page with specified identity.
     *
     * @param identity element identity for search
     */
    @Then("I get '$identity' element")
    @Alias("'$elementId' element")
    public void verifyElement(final String identity) {
        assertThat("Page element should exists: '" + identity,
                browser.base().find(identity),
                is(notNullValue()));
    }

    /**
     * Verify that HTML element with specified identity does not exist
     * on the current page .
     *
     * @param identity element identity for search
     */
    @Then("I get no '$identity' element")
    @Alias("no '$elementId' element")
    public void verifyElementIsNotPresent(final String identity) {
        browser.base().getWait().until(
                ExpectedConditions.invisibilityOfElementLocated(
                        browser.base().parseBy(identity)));
    }

    /**
     * Search HTML element on the current page with specified identity and tag
     * name.
     *
     * @param identity element identity for search
     */
    @Then("I get '$identity' link")
    @Alias("'$identity' link")
    public void verifyLink(final String identity) {
        assertThat("Page element should exists: '" + identity,
                browser.base().find(identity).getTagName(),
                is(equalTo("a")));
    }

    /**
     * Search HTML element on the current page with specified identity.
     *
     * @param identity element identity for search
     * @param text     the text for verification
     */
    @Then("I get '$identity' with '$text'")
    @Alias("'$identity' with '$text'")
    public void verifyElementText(final String identity, final String text) {
        String textFromStorageIfExist = getTextFromStorageIfExist(
                Resources.base().string(text, text));
        browser.base().find(identity).shouldHaveText(textFromStorageIfExist);
    }

    /**
     * Store text from element on the current page with specified identity,
     * to test session map.
     *
     * @param identity element identity for search and get text
     * @param key      for store and get text from session map
     */
    @When("I remember text from '$identity' to '$key' variable")
    public void storeTextFromElement(final String identity, final String key) {
        Resources.context().put(key, browser.base().find(identity).getText());
    }

    /**
     * Delete user session cookie and refresh the page.
     */
    @When("user session is expired")
    public void expireUserSession() {
        browser.base().manage().deleteCookieNamed(
                Resources.base().userSessionCookieName());
        browser.base().navigate().refresh();
    }

    /**
     * Switch to a new window.
     */
    @When("I switch to new window")
    public void switchToNewWindow() {
        final WebDriver driver = browser.base();
        final String baseWindowHandle = driver.getWindowHandle();
        final Set<String> opened = driver.getWindowHandles();
        String newWindow;
        if (opened.size() > 1 && opened.remove(baseWindowHandle)) {
            final Iterator<String> iterator = opened.iterator();
            newWindow = iterator.next();
        } else {
            final WebDriverWait wait =
                    new WebDriverWait(driver, Resources.base().explicitWait());
            newWindow = wait.until(WebExpectedConditions.get()
                    .anyWindowOtherThan(opened));
        }
        driver.switchTo().window(newWindow);
    }

}
