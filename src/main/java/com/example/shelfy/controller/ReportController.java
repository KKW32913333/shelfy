package com.example.shelfy.controller;

import com.example.shelfy.model.ConsumptionLog;
import com.example.shelfy.repository.ConsumptionLogRepository;
import com.example.shelfy.repository.ShelfyItemRepository;
import com.example.shelfy.service.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ReportController {

    private final ConsumptionLogRepository consumptionRepo;
    private final ShelfyItemRepository itemRepository;
    private final CurrentUserService currentUserService;

    public ReportController(ConsumptionLogRepository consumptionRepo,
                            ShelfyItemRepository itemRepository,
                            CurrentUserService currentUserService) {
        this.consumptionRepo = consumptionRepo;
        this.itemRepository = itemRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/report")
    public String report(@RequestParam(required = false) String month, Model model) {
        Long groupId = currentUserService.getCurrentGroupId();

        YearMonth ym = (month != null && !month.isBlank())
            ? YearMonth.parse(month)
            : YearMonth.now();

        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        List<ConsumptionLog> allLogs = consumptionRepo.findByGroupIdOrderByConsumedAtDesc(groupId);

        List<ConsumptionLog> monthLogs = allLogs.stream()
            .filter(l -> {
                LocalDate d = l.getConsumedAt().toLocalDate();
                return !d.isBefore(start) && !d.isAfter(end);
            }).collect(Collectors.toList());

        int totalConsumed = monthLogs.stream().mapToInt(ConsumptionLog::getQuantity).sum();

        long expiredCount = itemRepository.findByGroupIdOrderByUpdatedAtDesc(groupId).stream()
            .filter(i -> i.getExpiryDate() != null && i.getExpiryDate().isBefore(LocalDate.now()))
            .count();

        Map<String, Long> categoryCount = monthLogs.stream()
            .collect(Collectors.groupingBy(
                l -> l.getCategory() != null ? l.getCategory() : "other",
                Collectors.summingLong(ConsumptionLog::getQuantity)
            ));

        List<Map<String, Object>> dailyData = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d");
        for (int i = 1; i <= ym.lengthOfMonth(); i++) {
            LocalDate date = ym.atDay(i);
            final LocalDate d = date;
            long count = monthLogs.stream()
                .filter(l -> l.getConsumedAt().toLocalDate().equals(d))
                .mapToLong(ConsumptionLog::getQuantity).sum();
            if (count > 0) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("date", date.format(fmt));
                entry.put("count", count);
                dailyData.add(entry);
            }
        }

        model.addAttribute("ym", ym);
        model.addAttribute("month", ym.toString());
        model.addAttribute("prevMonth", ym.minusMonths(1).toString());
        model.addAttribute("nextMonth", ym.plusMonths(1).toString());
        model.addAttribute("totalConsumed", totalConsumed);
        model.addAttribute("expiredCount", expiredCount);
        model.addAttribute("monthLogs", monthLogs);
        model.addAttribute("categoryCount", categoryCount);
        model.addAttribute("dailyData", dailyData);

        return "report";
    }
}
