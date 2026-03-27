package com.pixology.backend.ui;

import org.openqa.selenium.chrome.ChromeOptions;

final class UiOpts {
    static ChromeOptions options() {
        ChromeOptions opts = new ChromeOptions();
        String headless = System.getProperty("HEADLESS", "false"); // default: show browser
        if ("true".equalsIgnoreCase(headless)) {
            opts.addArguments("--headless=new");
        }
        opts.addArguments("--window-size=1280,900");
        // useful on some CI agents
        opts.addArguments("--disable-dev-shm-usage", "--no-sandbox");
        return opts;
    }
}
