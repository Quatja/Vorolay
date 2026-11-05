# Vorolay

A small Android ViewGroup that displays child views inside Voronoi regions. The library has no runtime dependencies and supports Android 4.2 (API 17) and newer.

## Build and verify

Use JDK 17 and Android SDK 36:

```shell
./gradlew :app:assembleDebug :vorolay:assembleRelease :app:lintDebug :vorolay:lintDebug
./gradlew :app:connectedDebugAndroidTest  # requires a device or emulator, API 21+
./gradlew :vorolay:publishReleasePublicationToVerificationRepository
./gradlew -p integration-tests/consumer assembleRelease
```

The release AAR is written to `vorolay/build/outputs/aar/`. The publication task writes the AAR, sources and Maven metadata to `build/verification-repository/`. The independent consumer resolves the published artifact from that repository and builds a minified release with R8. Its test APK is signed with the debug key solely for device checks.

## Installation

The current source has not yet been published as a new release; existing JitPack versions refer to older code. To use this checkout immediately, run the publication task above and add the resulting repository to the consuming project's `settings.gradle.kts`, replacing the example path with the absolute path to this checkout:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("/absolute/path/to/Vorolay/build/verification-repository") }
    }
}
```

Then add to the consuming module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.Quatja:vorolay:1.1.0")
}
```

Alternatively, include the `vorolay` source module and use `implementation(project(":vorolay"))`. The checked-in consumer under `integration-tests/consumer` is a complete example of the local Maven approach.

### JitPack release

The repository includes `jitpack.yml` and a Maven publication for JitPack. After committing and pushing the changes, create and push a new Git tag, request its build on [JitPack](https://jitpack.io/#Quatja/Vorolay), and verify the result before advertising it. For a multi-module JitPack build, the intended dependency is `com.github.Quatja.Vorolay:vorolay:<tag>` with `maven { url = uri("https://jitpack.io") }` in the consumer's repositories. This route has not been verified until JitPack builds the new tag. Do not reuse a version already published with different contents.

## Usage

Place the view in a layout:

```xml
<quatja.com.vorolay.VoronoiView
    android:id="@+id/voronoi"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:show_border="true"
    app:border_color="#ffffff"
    app:border_width="3.5"
    app:generation_type="random" />
```

Declare `xmlns:app="http://schemas.android.com/apk/res-auto"` on the root element. Add children normally with `addView`; regions update when children or container size change. `GONE` children do not occupy a region. `refresh()` generates a new diagram.

Use `setGenerationType` to choose `GENERATION_TYPE_RANDOM`, `GENERATION_TYPE_ORDERED`, or `GENERATION_TYPE_CUSTOM`. For custom mode, call `setCustomPoints` with one distinct point per visible child, in child order. Coordinates are pixels relative to the content area inside padding. Set the points and generation type before the next layout. The region click listener receives the child view and its index in the ViewGroup; clickable children handle their own clicks.

Custom points must be within the content bounds; invalid points or a mismatched count cause `IllegalArgumentException` during layout. Call view methods on the Android UI thread.

For a fixed set of children, custom points can also be supplied as a string-array resource:

```xml
<quatja.com.vorolay.VoronoiView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:generation_type="custom"
    app:custom_points="@array/voronoi_points">
    <!-- Three child views -->
</quatja.com.vorolay.VoronoiView>
```

```xml
<string-array name="voronoi_points">
    <item>0.25, 0.25</item>
    <item>0.75, 0.25</item>
    <item>0.50, 0.75</item>
</string-array>
```

Each XML entry is an `x, y` pair of numbers from 0 to 1, inclusive, relative to the content area inside padding. The points scale when the content size changes. They map in order to children that are not `GONE` (including `INVISIBLE` children), and must be distinct. Malformed entries and out-of-range coordinates are rejected when the view is created; the count and uniqueness are checked during layout. Calling `setCustomPoints`, including with `null`, replaces the XML points; the method continues to use pixel coordinates.

Border attributes are `show_border`, `border_color`, `border_width` (pixels), and `border_round`. Corresponding methods are available on `VoronoiView`.

## License

Apache License 2.0; see [LICENSE](LICENSE). The original Voronoi algorithm includes additional attribution and permission notices in its source file.
