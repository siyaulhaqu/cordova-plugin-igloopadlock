#!/usr/bin/env node

module.exports = function (context) {
    var fs = require('fs');
    var path = require('path');

    var buildGradlePath = path.join(context.opts.projectRoot, 'platforms/android/app/build.gradle');
    if (!fs.existsSync(buildGradlePath)) {
        console.warn("build.gradle not found at expected location:", buildGradlePath);
        return;
    }

    var buildGradle = fs.readFileSync(buildGradlePath, 'utf-8');
    if (buildGradle.includes('packagingOptions')) {
        console.log("packagingOptions already configured.");
        return;
    }

    var insertBlock = `
    packagingOptions {
        pickFirst 'lib/armeabi-v7a/libble-sdk.so'
        pickFirst 'lib/x86/libble-sdk.so'
        pickFirst 'lib/arm64-v8a/libble-sdk.so'
        pickFirst 'lib/x86_64/libble-sdk.so'
        pickFirst 'META-INF/kotlin-stdlib.kotlin_module'
        pickFirst 'META-INF/kotlin-stdlib-common.kotlin_module'
        pickFirst 'META-INF/kotlin-stdlib-jdk7.kotlin_module'
        pickFirst 'META-INF/kotlin-stdlib-jdk8.kotlin_module'
        pickFirst 'META-INF/annotations.kotlin_module'
    }`;

    var androidIndex = buildGradle.indexOf("android {");
    if (androidIndex !== -1) {
        var insertPos = buildGradle.indexOf("{", androidIndex) + 1;
        buildGradle = buildGradle.slice(0, insertPos) + insertBlock + buildGradle.slice(insertPos);
        fs.writeFileSync(buildGradlePath, buildGradle);
        console.log("Injected packagingOptions into build.gradle");
    } else {
        console.warn("Could not find 'android {' block in build.gradle.");
    }
};
