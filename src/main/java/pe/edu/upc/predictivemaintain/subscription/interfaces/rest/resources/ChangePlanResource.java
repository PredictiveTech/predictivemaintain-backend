package pe.edu.upc.predictivemaintain.subscription.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangePlanResource(@NotNull UUID planId) {
}