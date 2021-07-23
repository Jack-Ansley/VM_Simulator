# VM_Simulator
This Java program simulates two different virtual memory allocation algorithms working across two different processes, and outputs data accordingly.

After compilation, the program is ran with the following Syntax:
```shell
./vmsim -a <opt|lru> –n <numframes> -p <pagesize in KB> -s <memory split> <tracefile>
```
'opt' refers to an optimal page replacement algorithm. It will scan through the trace files before gathering data and will optimally choose which to evict or keep.
'lru' refers to the 'least recently used' page replacement algorithm.

'numframes' refers to the number of frames that are available for a page to occupy

'memory split' refers to the ratio in which the frames will be allocated across the two different processes.

The tracefiles to run this program are also provided in the repository.
