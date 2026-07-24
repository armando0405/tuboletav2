package com.tuboleta.backend.api.controllers;

import com.tuboleta.backend.api.dtos.NotificationResponse;
import com.tuboleta.backend.config.security.AppUserPrincipal;
import com.tuboleta.backend.service.InboxService;
import com.tuboleta.backend.utils.constants.ErrorCode;
import com.tuboleta.backend.utils.constants.ErrorMessage;
import com.tuboleta.backend.utils.response.ObjectListResponse;
import com.tuboleta.backend.utils.response.ObjectResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbox in-app (REQ-NOT-003): lectura y marcado de leído, nunca automático
 * por listar.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final InboxService inboxService;

    public NotificationController(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    @GetMapping
    public ObjectListResponse<NotificationResponse> list(@AuthenticationPrincipal AppUserPrincipal principal,
                                                           @RequestParam(required = false, defaultValue = "false") boolean unreadOnly) {
        return new ObjectListResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS,
                inboxService.listMine(principal.getId(), unreadOnly));
    }

    @GetMapping("/unread-count")
    public ObjectResponse<Long> unreadCount(@AuthenticationPrincipal AppUserPrincipal principal) {
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS, inboxService.unreadCount(principal.getId()));
    }

    @PostMapping("/{id}/read")
    public ObjectResponse<Void> markRead(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        inboxService.markRead(principal.getId(), id);
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS);
    }

    @PostMapping("/read-all")
    public ObjectResponse<Void> markAllRead(@AuthenticationPrincipal AppUserPrincipal principal) {
        inboxService.markAllRead(principal.getId());
        return new ObjectResponse<>(ErrorCode.SUCCESS, ErrorMessage.SUCCESS);
    }
}
