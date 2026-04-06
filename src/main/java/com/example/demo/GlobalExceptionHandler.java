package com.example.demo;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // Find referring URL (like /admin) to redirect back cleanly
        String referrer = request.getHeader("referer");
        redirectAttributes.addFlashAttribute("error", "File too large! Must be under 5MB.");
        
        if (referrer != null && referrer.contains("/admin")) {
            return "redirect:/admin";
        }
        return "redirect:/?error=file_too_large";
    }

    // You can add more generic handlers here (e.g. ConstraintViolationException)
}
