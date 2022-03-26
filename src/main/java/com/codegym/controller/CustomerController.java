package com.codegym.controller;

import com.codegym.model.Customer;
import com.codegym.model.Province;
import com.codegym.service.customer.ICustomerService;
import com.codegym.service.provice.IProvinceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
public class CustomerController {
    @Autowired
    private ICustomerService customerService;
    @Autowired
    private IProvinceService provinceService;

    @ModelAttribute("provinces")
    public Iterable<Province> provinces(){
        return provinceService.findAll();
    }


    @GetMapping("/customers")
    public ModelAndView showAllCustomer(@PageableDefault(value = 5) Pageable pageable, @RequestParam("search") Optional<String>search) {
        Page<Customer> customers;
        if (search.isPresent()){
            customers = customerService.findAllByFirstNameContaining(search.get(), pageable);
        }
        else {
            customers = customerService.findAll(pageable);
        }
        ModelAndView modelAndView = new ModelAndView("/customer/list");
        modelAndView.addObject("customers", customers);
        return modelAndView;
    }

    @GetMapping("/customers/create")
    public ModelAndView showCreteForm() {
        ModelAndView modelAndView = new ModelAndView("customer/create");
        modelAndView.addObject("customer", new Customer());
        return modelAndView;
    }

    @PostMapping("/customers/create")
    public ModelAndView addNewCustomer(@ModelAttribute Customer customer) {
        customerService.save(customer);
        return new ModelAndView("redirect:/customers");
    }

    @GetMapping("/customers/edit/{id}")
    public ModelAndView showEditForm(@PathVariable Long id) {
        Optional<Customer> customer = customerService.findById(id);
        if (!customer.isPresent()) {
            return new ModelAndView("error-404");
        }
        ModelAndView modelAndView = new ModelAndView("customer/edit");
        modelAndView.addObject("customer", customer.get());
        return modelAndView;
    }

    @PostMapping("/customers/edit")
    public ModelAndView updateCustomer(@ModelAttribute("customer") Customer customer) {
        customerService.save(customer);
        return new ModelAndView("redirect:/customers");
    }

    @GetMapping("/customers/delete/{id}")
    public ModelAndView showDeleteForm(@PathVariable Long id) {
        Optional<Customer> customer = customerService.findById(id);
        if (!customer.isPresent()) {
            return new ModelAndView("error-404");
        }
        ModelAndView modelAndView = new ModelAndView("customer/delete");
        modelAndView.addObject("customer", customer.get());
        return modelAndView;
    }

    @PostMapping("/customers/delete")
    public ModelAndView removeCustomer(@ModelAttribute("customer") Customer customer) {
        customerService.removeById(customer.getId());
        return new ModelAndView("redirect:/customers");
    }

}
