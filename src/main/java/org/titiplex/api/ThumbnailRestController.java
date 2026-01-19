package org.titiplex.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.titiplex.service.ThumbnailService;

@RestController
@RequestMapping("/api/thumbnail")
public class ThumbnailRestController {

    private ThumbnailService thumbnailService;
}
