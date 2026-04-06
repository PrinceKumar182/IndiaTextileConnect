package com.example.demo;

import com.example.demo.dto.CarouselMediaDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminCarouselController {

    @Autowired
    private CarouselMediaRepository carouselMediaRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/addCarouselMedia")
    public String addCarouselMedia(@Valid CarouselMediaDto mediaDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin";
        }

        try {
            List<String> allowedExtensions = mediaDto.getType().equalsIgnoreCase("IMAGE") 
                    ? List.of("jpg", "jpeg", "png", "webp") 
                    : List.of("mp4", "webm");

            String fileUrl = fileStorageService.saveFile(mediaDto.getMediaFile(), allowedExtensions);
            
            CarouselMedia media = new CarouselMedia(
                    fileUrl, 
                    mediaDto.getTitle(), 
                    mediaDto.getSubtitle(), 
                    mediaDto.getType().toUpperCase(), 
                    mediaDto.getOrderIndex()
            );

            carouselMediaRepository.save(media);
            redirectAttributes.addFlashAttribute("success", "Media added to Carousel successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload media.");
        }

        return "redirect:/admin";
    }

    @PostMapping("/deleteCarouselMedia")
    public String deleteCarouselMedia(@RequestParam String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<CarouselMedia> mediaOpt = carouselMediaRepository.findById(id);
            if (mediaOpt.isPresent()) {
                fileStorageService.deleteFile(mediaOpt.get().getMediaUrl()); // Cleanup orphaned file
                carouselMediaRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Carousel media deleted.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete media.");
        }
        return "redirect:/admin";
    }
}
