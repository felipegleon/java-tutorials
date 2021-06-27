package com.tutorial.springbootreactive.tutorial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BasicConcepts  implements CommandLineRunner {

    private final static Logger LOG = LoggerFactory.getLogger(BasicConcepts.class);

    @Override
    public void run(String... args) throws Exception {

		/*
		LOG.info("####### firstFlux #########");
		firstFlux();
		LOG.info("####### methodSubscribe #########");
		methodSubscribe();
		LOG.info("####### eventOnComplete #########");
		eventOnComplete();
		LOG.info("####### methodMapInFlux #########");
		methodMapInFlux();
		LOG.info("####### filterOperatorInFlux #########");
		filterOperatorInFlux();
		LOG.info("####### methodFlatMap #########");
		methodFlatMap();
		LOG.info("####### combiningFluxesWithFlatmap #########");
		combiningFluxesWithFlatmap();
		LOG.info("####### combiningFluxesWithZipWith #########");
		combiningFluxesWithZipWith();
		LOG.info("####### combiningFluxesWithZipWithCase2 #########");
		combiningFluxesWithZipWithCase2();
		LOG.info("####### methodRange #########");
		methodRange();
		LOG.info("####### methodInterval #########");
		methodInterval();
		 */
        LOG.info("####### methodDelayElements #########");
        methodDelayElements();



    }

    /**
     * It is a data stream immutable where we have an observable for each item in the stream. When an element in the
     * stream change the is when the observable can see the change of the object. Then we must subscribe to the flux for
     * can see the changes
     */
    public void firstFlux () {
        Flux<String> names = Flux.just("Mike" , "Alban" , "Manou" , "Wafu")
                .doOnNext(System.out::println);

        names.subscribe();
    }

    /**
     * In this section can see that the subscribe method  is observing the elements in the Flux.
     */
    public void methodSubscribe () {
        Flux<String> names = Flux.just("Mike" , "Alban" , "", "Manou" , "Wafu")
                .doOnNext(element ->{
                    if (element.isEmpty()){
                        throw  new RuntimeException("The name can't be empty.");
                    }
                    System.out.println(element);
                });

        names.subscribe(element -> LOG.info("Name: " + element),
                error -> LOG.error(error.getMessage()));
    }

    /**
     * Is a event that is call when the Flux do finished.
     */
    public void	eventOnComplete () {
        Flux<String> names = Flux.just("Mike" , "Alban" , "Manou" , "Wafu")
                .doOnNext(element ->{
                    if (element.isEmpty()){
                        throw  new RuntimeException("The name can't be empty.");
                    }
                    System.out.println(element);
                });

        names.subscribe(element -> LOG.info("Name: " + element),
                error -> LOG.error(error.getMessage()),
                new Runnable() {
                    @Override
                    public void run() {
                        LOG.info("The Flux do finished");
                    }
                });
    }

    /**
     * is a method that allow transform the elements in a Flux and  return other Flux with the new objects transformed.
     */
    public void methodMapInFlux () {
        Flux<User> flux = Flux.just("Mike" , "Alban" , "Manou" , "Wafu")
                .map(name -> new User(name.toUpperCase(), null))
                .doOnNext(user ->{
                    if (user == null){
                        throw  new RuntimeException("The name can't be empty.");
                    }
                    System.out.println(user.getFirstName());
                })
                .map(user -> {
                    String name = user.getFirstName();
                    user.setFirstName(name.toLowerCase());
                    return user;
                });

        flux.subscribe(user -> LOG.info("Name: " + user.getFirstName()),
                error -> LOG.error(error.getMessage()),
                new Runnable() {
                    @Override
                    public void run() {
                        LOG.info("The Flux do finished");
                    }
                });
    }

    /**
     * This operator allow filter the elements in a Flux that comply  a condition, the return is a new Flux with the
     * objects filtered.
     */
    public void filterOperatorInFlux () {
        Flux<User> flux = Flux.just("Mike Towers" , "Alban Charles" , "Manou Lafone" , "Wafu Per")
                .map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
                .filter(user -> user.getFirstName().equalsIgnoreCase("Manou"))
                .doOnNext(user ->{
                    if (user == null){
                        throw  new RuntimeException("The name can't be empty.");
                    }
                    System.out.println(user.getFirstName());
                })
                .map(user -> {
                    String name = user.getFirstName();
                    user.setFirstName(name.toLowerCase());
                    return user;
                });

        flux.subscribe(user -> LOG.info("Name: " + user.getFirstName() + " " + user.getLastName()),
                error -> LOG.error(error.getMessage()),
                new Runnable() {
                    @Override
                    public void run() {
                        LOG.info("The Flux do finished");
                    }
                });
    }

    /**
     * Here the flatMap method unlike the filter is that it returns an observable and not the object simple.
     */
    public void methodFlatMap () {

        List<String> users = new ArrayList<>();
        users.add("Mike Towers");
        users.add("Alban Charles");
        users.add("Manou Lafone");
        users.add( "Wafu Per");
        users.add("Manou Two");

        Flux.fromIterable(users)
                .map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
                .flatMap(user -> {
                    if(user.getFirstName().equalsIgnoreCase("Manou")){
                        return Mono.just(user);
                    }else{
                        return Mono.empty();
                    }
                })
                .map(user ->{
                    String name = user.getFirstName().toLowerCase();
                    user.setFirstName(name);
                    return user;
                })
                .subscribe(user -> LOG.info(user.toString()));
    }

    public void combiningFluxesWithFlatmap (){
        Mono<User> userMono = Mono.fromCallable(() -> new User("Manou", "Lafone"));
        Mono<Comments> commentsMono =  Mono.fromCallable(() ->{
            Comments comments = new Comments();
            comments.addComment("Hi pipe, how are you?");
            comments.addComment("Tomorrow I will go to the beach!");
            return comments;
        });

        userMono.flatMap(user -> commentsMono.map(comments -> new UserWithComments(user, comments)))
                .subscribe(userWithComments -> LOG.info(userWithComments.toString()));
    }

    public void combiningFluxesWithZipWith(){
        Mono<User> userMono = Mono.fromCallable(() -> new User("Manou", "Lafone"));
        Mono<Comments> commentsMono =  Mono.fromCallable(() ->{
            Comments comments = new Comments();
            comments.addComment("Hi pipe, how are you?");
            comments.addComment("Tomorrow I will go to the beach!");
            return comments;
        });
        Mono<UserWithComments> userWithCommentsMono = userMono.zipWith(commentsMono, (user, comments) -> new UserWithComments(user, comments));
        userWithCommentsMono.subscribe(userWithComments -> LOG.info(userWithComments.toString()));
    }

    public void combiningFluxesWithZipWithCase2(){
        Mono<User> userMono = Mono.fromCallable(() -> new User("Manou", "Lafone"));
        Mono<Comments> commentsMono =  Mono.fromCallable(() ->{
            Comments comments = new Comments();
            comments.addComment("Hi pipe, how are you?");
            comments.addComment("Tomorrow I will go to the beach!");
            return comments;
        });
        Mono<UserWithComments> userWithCommentsMono = userMono
                .zipWith(commentsMono)
                .map(tuple ->{
                    User user = tuple.getT1();
                    Comments comments = tuple.getT2();
                    return new UserWithComments(user, comments);
                });
        userWithCommentsMono.subscribe(userWithComments -> LOG.info(userWithComments.toString()));
    }

    /**
     * Create a Flux with a range, example 0 to 4.
     */
    public void methodRange () {
        Flux.just(1, 2, 3, 4)
                .map(number -> (number * 2))
                .zipWith(Flux.range(0, 4), (firstFlux, secondFlux) -> String.format("First flux: %d, Second flux %d", firstFlux, secondFlux))
                .subscribe(text -> LOG.info(text));
    }

    public  void methodInterval (){
        Flux<Integer> rangeIntegerFlux = Flux.range(1, 12);
        Flux<Long> delayFlux = Flux.interval(Duration.ofSeconds(1));

        rangeIntegerFlux.zipWith(delayFlux, (range, delay) -> range)
                .doOnNext(i -> LOG.info(i.toString()))
                .subscribe();
    }

    public  void methodDelayElements (){
        Flux<Integer> rangeIntegerFlux = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> LOG.info(i.toString()));
        rangeIntegerFlux.subscribe();
    }
}
