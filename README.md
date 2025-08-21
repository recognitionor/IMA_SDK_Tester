# IMA_SDK_Tester

테스트 순서
* com.google.ads.interactivemedia.v3:interactivemedia:3.33.0 libs 이용
* 최초의 진입 Activity TestActivity
* TestActivity 에서 MainActivity 를 띄웁니다.
  *  MainActivity 에서 
  ImaSdkFactory.getInstance().createAdsLoader(this, settings, adDisplayContainer)
  위 코드를 생성하고 MainActivity 를 종료
* Found 2 objects retained, dumping heap now (app is invisible) 로그가 카나리 로그에 찍힘
* 위 동작을 몇번 반복하면 2개씩 Found 2 objects retained 증가하며 5가 넘어가면 메모리릭 보고를 진행 하게 됩니다.

해결
무슨 짓을 해도 어떤 방식으로 해도 메모리릭 방어가 되지 않음
com.google.ads.interactivemedia.v3:interactivemedia:3.36.0
로 버전을 올리면 해당 문제가 발생 하지 않음
심지어 release 및 리소스 반환 하는 로직을 진행 하지 별도로 하지 않아도 발생 하지 않음