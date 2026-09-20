import org.junit.jupiter.api.*;
import org.labs.Programmer;
import org.labs.Table;

import java.util.Arrays;
import java.util.IntSummaryStatistics;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    @Nested
    @DisplayName("Работа стола")
    class TableOperation {

        private static final int SEATS = 14;
        private static final int WAITERS = 5;
        private static final int THRESHOLD = 2;
        private static final int SIDES = 100;
        private static final int REPEATS = 5;

        @RepeatedTest(
                value = REPEATS,
                name = "Запуск {currentRepetition} из {totalRepetitions}"
        )
        @Timeout(5)
        @DisplayName("Случайное время приёма пищи")
        void randomEatTime() {
            Table table = new Table(
                    SEATS,
                    WAITERS,
                    THRESHOLD,
                    SIDES
            );

            Programmer[] result = table.start();

            assertAllTableInvariants(result);
        }

        @RepeatedTest(
                value = REPEATS,
                name = "Запуск {currentRepetition} из {totalRepetitions}"
        )
        @Timeout(5)
        @DisplayName("Одинаковое время приёма пищи")
        void equalEatTime() {
            Table table = new Table(
                    SEATS,
                    WAITERS,
                    THRESHOLD,
                    SIDES,
                    10
            );

            Programmer[] result = table.start();

            assertAllTableInvariants(result);
        }

        private void assertAllTableInvariants(Programmer[] result) {
            assertAll(
                    () -> assertSidesCount(result),
                    () -> assertThreshold(result)
            );
        }

        private void assertSidesCount(Programmer[] result) {
            int actualSides = Arrays.stream(result)
                    .mapToInt(Programmer::getTotalSides)
                    .sum();

            assertEquals(
                    SIDES,
                    actualSides,
                    "Общее количество порций изменилось"
            );
        }

        private void assertThreshold(Programmer[] result) {
            IntSummaryStatistics statistics = Arrays.stream(result)
                    .mapToInt(Programmer::getTotalSides)
                    .summaryStatistics();

            int difference = statistics.getMax() - statistics.getMin();

            assertTrue(
                    difference <= THRESHOLD,
                    () -> """
                            Разница между максимальным и минимальным \
                            количеством порций должна быть не больше %d, \
                            фактическая разница: %d
                            """.formatted(THRESHOLD, difference)
            );
        }
    }
}