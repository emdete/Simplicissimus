#!/usr/bin/env make -f
dirs := $(shell dirname */Makefile)

all:
	$(foreach dir,$(dirs),$(MAKE) -C $(dir) all && ) true

run:
	$(foreach dir,$(dirs),$(MAKE) -C $(dir) run;)

clean:
	$(foreach dir,$(dirs),$(MAKE) -C $(dir) clean;)

