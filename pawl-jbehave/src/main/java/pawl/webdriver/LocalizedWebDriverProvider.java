/*
 * Copyright 2014 Geeoz Software
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

package pawl.webdriver;

import org.jbehave.web.selenium.PropertyWebDriverProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import pawl.util.Resources;

import java.util.Locale;

/**
 * Provide localization support for different browsers.
 *
 * @author Mike Dolinin
 * @version 1.1 10/11/14
 */
public class LocalizedWebDriverProvider extends PropertyWebDriverProvider {
    /**
     * User language property name.
     */
    public static final String LANGUAGE = "user.language";
    /**
     * User country property name.
     */
    public static final String COUNTRY = "user.country";

    /**
     * Provide new Firefox driver with setup of user language.
     *
     * @return firefox driver
     */
    protected FirefoxDriver createFirefoxDriver() {
        FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("intl.accept_languages",
                getSystemLanguage());
        FirefoxDriver firefoxDriver = new FirefoxDriver(firefoxProfile);
        firefoxDriver.manage().window().maximize();
        return firefoxDriver;
    }

    /**
     * Provide new PhantomJS driver with setup of user language.
     *
     * @return phantomjs driver
     */
    protected WebDriver createPhantomJSDriver() {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        String[] phantomArgs = new String[]{
                "--webdriver-loglevel="
                        + Resources.base().webDriverLogLevel()
        };
        desiredCapabilities
                .setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                        phantomArgs);
        desiredCapabilities.setCapability(
                "phantomjs.page.customHeaders.Accept-Language",
                getSystemLanguage());
        PhantomJSDriver phantomJSDriver = new PhantomJSDriver(
                desiredCapabilities);
        phantomJSDriver.manage().window().maximize();
        return phantomJSDriver;
    }

    /**
     * Provide system language key.
     *
     * @return language key
     */
    private String getSystemLanguage() {
        String language;
        if (System.getProperty(COUNTRY) == null) {
            language = System.getProperty(LANGUAGE);
        } else {
            language = System.getProperty(LANGUAGE) + "-"
                    + System.getProperty(COUNTRY).toLowerCase(Locale.ENGLISH);
        }
        return language;
    }
}
