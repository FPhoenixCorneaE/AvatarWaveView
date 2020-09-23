# AvatarWaveView
头像随波浪漂移效果


How to include it in your project:
--------------
**Step 1.** Add the JitPack repository to your build file
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

**Step 2.** Add the dependency
```groovy
dependencies {
    implementation 'com.github.FPhoenixCorneaE:AvatarWaveView:1.0.1'
}
```



**代码中使用**
```kotlin
private var avatarWaveHelper: AvatarWaveHelper? = null
avatarWaveHelper = AvatarWaveHelper(
            wvHeader,
            ivAvatar,
            Color.parseColor("#80FC7A8C"),
            Color.parseColor("#40FB3D53")
        )
```

**Activity生命周期中**
```kotlin
override fun onPause() {
        super.onPause()
        avatarWaveHelper?.cancel()
    }
```

```kotlin
override fun onResume() {
        super.onResume()
        avatarWaveHelper?.start()
    }
```