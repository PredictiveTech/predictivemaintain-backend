package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record SyncRequestResource(
        @NotNull
        @Schema(description = "What time it is on the DEVICE's clock at the moment of sending. The server compares "
                + "it with its own clock to translate every time in the batch")
        Instant deviceSentAt,
        @NotEmpty @Size(max = 100) @Valid
        List<SyncOperationResource> operations) {
}