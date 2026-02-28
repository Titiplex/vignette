package org.titiplex.api.dto;

public record AudioRowDto(Long id,
                          String title,
                          Integer idx,
                          String mime,
                          Double markerX,
                          Double markerY,
                          String markerLabel) {
}
