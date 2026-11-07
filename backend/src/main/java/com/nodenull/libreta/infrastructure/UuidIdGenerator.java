package com.nodenull.libreta.infrastructure;

import com.nodenull.libreta.application.IdGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Genera identificadores UUID. */
@Component
public class UuidIdGenerator implements IdGenerator {

    @Override
    public String next() {
        return UUID.randomUUID().toString();
    }
}
