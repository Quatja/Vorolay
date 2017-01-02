# Vorolay
<br>
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![](https://jitpack.io/v/Quatja/Vorolay.svg)](https://jitpack.io/#Quatja/Vorolay)
<br>
<br>
VoronoiView is a view (ViewGroup) that allows you to add and display views inside Voronoi diagram regions. [Voronoi diagram] (https://en.wikipedia.org/wiki/Voronoi_diagram)


## Screenshots
![alt text](https://github.com/Quatja/Vorolay/raw/master/Screenshots/screenshot_1.png "Simple diagram")
![alt text](https://github.com/Quatja/Vorolay/raw/master/Screenshots/screenshot_2.png "Settings")
![alt text](https://github.com/Quatja/Vorolay/raw/master/Screenshots/screenshot_3.png "List view")
![alt text](https://github.com/Quatja/Vorolay/raw/master/Screenshots/screenshot_4.png "Custom views")



## Import
Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        compile 'com.github.Quatja:Vorolay:1.0.1'
	}

<br>
---------

## Usage

Add the view into your layout. You can customize it by using xml attributes

```xml
<quatja.com.vorolay.VoronoiView
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  android:layout_margin="50dp"
  custom:border_color="@color/gray"
  custom:border_round="false"
  custom:border_width="5"    />
```

Then add your views to the VoronoiView

```java
VoronoiView voronoiView = (VoronoiView) findViewById(R.id.voronoi);
LayoutInflater layoutInflater = getLayoutInflater();
for (int i = 0; i < 15; i++) {
    View view = layoutInflater.inflate(R.layout.item_voronoi, null, false);
    voronoiView.addView(view);
}
```

Now VoronoiView children will be displayed inside the diagram regions.




<br>
---------

## Customization

Attributes which you can use in customization purposes

| attr            | value/example  | description                                   |
| --------------- |:--------------:| ---------------------------------------------:|
| show_border     | true           | shows or hides each region borders            |
| border_color    | #fff           | sets all borders color                        |
| border_width    | 5              | sets borders width                            |
| border_round    | true           | makes border corners round                    |
| generation_type | random         | sets different points generation behavior     |


### Generation types
| type            | description                                                               |
| --------------- | -------------------------------------------------------------------------:|
| random          | VoronoiView generates a random point positions inside the view bounds.    |
| ordered         | points are placed like a table - rows and columns.                        |
| custom          | points will be placed in user defined positions.                          |

These attributes are available programmatically too.
<br>



---------

## Warning
Children visibility GONE doesn't work <br>
Do not use tags for the children <br>




---------

## Acknowledgements
Steven Fortune - algorithm author <br>
Zhenyu Pan - Forutne algorithm Java version <br>
Bart Kiers - Graham Scan realization <br>


---------

## License
```xml
Copyright 2016 Daniil Jurjev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
