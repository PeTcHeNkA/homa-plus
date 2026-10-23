package plus.tcord;


public interface TQueue {
    /**
     * Получить следующее значение, которое надо перевести
     * @return следующее значение или null если все переведено
     */
    String next();


    /**
     * @return true если завершено
     */
    boolean isCompleted();


    /**
     * Вызывается при ошибке перевода
     */
    void onError(String msg);


    /**
     * Вызывается после успешного завершения перевода
     * @param msg Оригинал
     * @param result Перевод
     */
    void onSuccess(String msg, String result);
}