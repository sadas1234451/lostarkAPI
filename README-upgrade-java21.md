이 저장소를 Java 21 (LTS)로 업그레이드하기 위한 최소 안내

요약
- `build.gradle`의 Gradle toolchain을 Java 21로 변경했습니다.
- 시스템에 JDK 21이 설치되어 있지 않으면 빌드/실행이 실패할 수 있습니다.

Windows에 JDK 21 설치 및 실행(권장)
1. AdoptOpenJDK / Eclipse Temurin(Adoptium) 또는 Oracle의 JDK 21을 다운로드하고 설치하세요.
   - Temurin: https://adoptium.net
2. 시스템 환경 변수 `JAVA_HOME`을 설치한 JDK 21 폴더로 설정하세요.
   - 예: C:\Program Files\Java\jdk-21
3. PowerShell에서 설치 확인:

   ```powershell
   java -version
   ```

4. 프로젝트 루트에서 Gradle wrapper로 실행:

   ```powershell
   cd C:\myProject\lostarkAPI
   .\gradlew.bat clean bootRun --no-daemon --stacktrace
   ```

참고
- 현재 개발 환경에는 Java 17이 사용 중이며 Gradle도 Java 17을 사용하고 있습니다. (출력: `java version "17.0.11"`)
- JDK 21을 설치하면 `java -version`이 21을 가리키도록 하거나 Gradle toolchain의 자동 설치/설정을 사용해야 합니다.
- 추가로 원하시면 `build.gradle`에 Gradle의 자동 JDK 다운로드 설정을 적용하거나 CI 환경을 업데이트해 드리겠습니다.
