package pe.edu.upc.predictivemaintain.iam.domain.model.commands;

public record ResetPasswordCommand(String token, String newPassword) {
}