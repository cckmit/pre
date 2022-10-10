package com.xd.pre.common.utils.px;

import cn.hutool.core.util.StrUtil;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ChromeDriverUtils {
    /**
     * 获取 getChromeDriver
     *
     * @param headless
     * @return
     */
    public ChromeDriver getChromeDriver(Boolean headless, String ip, String port,String path) {
        String proxyServer = String.format("%s:%s", ip, port);
        Proxy proxy = new Proxy().setHttpProxy(proxyServer).setSslProxy(proxyServer);
        System.setProperty("webdriver.chrome.driver", path);
//        System.setProperty("webdriver.chrome.driver", "/usr/local/google/chromedriver_99");
        Map<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", "Nexus 5");
        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.addArguments("blink-settings=imagesEnabled=false");//禁用图片
        chromeOptions.setHeadless(headless);
        if (StrUtil.isNotBlank(ip) && StrUtil.isNotBlank(port)) {
            chromeOptions.setProxy(proxy);
        }
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--auto-open-devtools-for-tabs");
        chromeOptions.setCapability("networkConnectionEnabled", true);
        chromeOptions.setExperimentalOption("w3c", false);
        chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        DesiredCapabilities caps = DesiredCapabilities.chrome();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        caps.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        return new ChromeDriver(caps);
    }

}
