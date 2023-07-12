import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ReactiveTutorial {
    public static void main(String[] args) throws InterruptedException {

        List<Integer> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(elements::add);
        System.out.println("elements = " + elements);

        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription subscription) {
                        subscription.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        elements.add(integer);
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        System.out.println("elements = " + elements);

        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(new Subscriber<Integer>() {
                    private Subscription s;
                    int onNextAmount;

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        this.s = subscription;
                        s.request(2L); // 2개씩만 consume 하도록 설정
                    }

                    @Override
                    public void onNext(Integer integer) {
                        elements.add(integer);

                        // 2번째 consume 때, 다시 s.request 를 통해 2개 consume 하도록 요청
                        onNextAmount++;
                        if (onNextAmount % 2 == 0) {
                            s.request(2L);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        System.out.println("elements = " + elements);

        Flux.just(1, 2, 3, 4)
                .log()
                .map(i -> i * 2) // onNext 가 호출되면 map 이 적용됨
                .subscribe(elements::add);
        System.out.println("elements = " + elements);

        List<String> elements2 = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .map(i -> i * 2)
                .zipWith(Flux.range(0, Integer.MAX_VALUE),
                        (one, two) -> String.format("First Flux: %d, Second Flux: %d", one, two))
                .subscribe(elements2::add);
        System.out.println("elements = " + elements2);

        ConnectableFlux<Object> publish = Flux.create(fluxSink -> {
            while (true) {
                fluxSink.next(System.currentTimeMillis());
            }
        })
                .sample(Duration.ofSeconds(2)) // consume 시, 2초 마다 onNext 호출되도록 설정
                .publish();

        publish.subscribe(System.out::println);
        // connect를 하지 않는 이상 consume 하지 않음
        // publish.connect();

        List<Integer> elements3 = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .map(i -> i * 2)
                .subscribeOn(Schedulers.parallel()) // 별도 쓰레드에서 실행되도록 설정
                .subscribe(elements3::add);
        System.out.println("elements3 = " + elements3);
        Thread.sleep(1000);
        System.out.println("elements3 = " + elements3);
    }
}
