# Baseline profiles

Use an unlocked Android 14+ physical device, Spaces as the startup screen, and app lock disabled.

## Install

```sh
./gradlew :androidApp:installNonMinifiedRelease
```

Open the app and add sample data.

## Generate profiles

```sh
./gradlew :androidApp:generateReleaseBaselineProfile \
  -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true
```

Commit `baseline-prof.txt` and `startup-prof.txt` from `androidApp/src/release/generated/baselineProfiles/`.

## Run startup benchmarks

```sh
./gradlew :baseline-profile:connectedBenchmarkReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.mhss.app.baseline_profile.StartupBenchmarks \
  -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true
```

Compare median `timeToInitialDisplayMs` with and without profiles. Each mode runs 10 cold starts; lower is better. Results are under `baseline-profile/build/outputs/connected_android_test_additional_output/benchmarkRelease/`.
