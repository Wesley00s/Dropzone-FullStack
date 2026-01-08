import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FileService, FileState} from '../../core/services/file.service';
import {FileSizePipe} from '../../shared/pipes/file-size.pipe';
import {ToastService} from '../../core/services/toast.service';
import {ConfirmService} from '../../core/services/confirm.service';

@Component({
  selector: 'app-file-manager',
  standalone: true,
  imports: [CommonModule, FileSizePipe],
  templateUrl: './file-manager.feature.html',
  styleUrl: './file-manager.feature.css',
})
export class FileManagerFeature implements OnInit {
  public fileService = inject(FileService);
  private confirmService = inject(ConfirmService);
  private toastService = inject(ToastService);
  isDragging = signal(false);

  state = signal<FileState>({
    loading: false,
    error: null,
    pagination: {data: [], pagination: {page: 0, size: 10, totalElements: 0, totalPages: 0, totalSize: 0}}
  });

  ngOnInit() {
    this.fileService.fileState$.subscribe((newState) => {
      this.state.set(newState);
    });
    this.loadFiles();
  }

  loadFiles(page = 0) {
    this.fileService.list(page, 10);
  }

  onPageChange(newPage: number) {
    this.loadFiles(newPage);
  }

  private handleUpload(files: File[]) {
    this.fileService.upload(files).subscribe({
      next: (uploadedFiles) => {
        const count = uploadedFiles.length;
        const msg = count > 1
          ? `${count} arquivos enviados com sucesso!`
          : 'Arquivo enviado com sucesso!';

        this.toastService.showSuccess(msg);

        this.loadFiles(0);
      },
      error: (err) => {
        this.toastService.showError('Erro ao realizar upload: ' + err.message);
      }
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const files = Array.from(input.files);
    this.handleUpload(files);

    input.value = '';
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(false);

    const transferred = event.dataTransfer;
    if (transferred && transferred.files.length > 0) {
      const files = Array.from(transferred.files);
      this.handleUpload(files);
    }
  }

  async onDelete(id: string) {
    const confirmed = await this.confirmService.ask(
      'Excluir Arquivo?',
      'Esta ação não pode ser desfeita. O arquivo será removido permanentemente.'
    );

    if (confirmed) {
      this.fileService.delete(id).subscribe({
        next: () => {
          this.toastService.showSuccess('Arquivo excluído com sucesso.');

          const currentData = this.state().pagination.data;
          const currentPage = this.currentPage;

          if (currentData.length === 1 && currentPage > 0) {
            this.loadFiles(currentPage - 1);
          } else {
            this.loadFiles(currentPage);
          }
        },
        error: (err) => {
          this.toastService.showError('Erro ao excluir arquivo.');
        }
      });
    }
  }

  onDownload(id: string) {
    this.fileService.getDownloadUrl(id).subscribe({
      next: (res) => {
        window.open(res.url, '_blank');
      },
      error: (e) => this.toastService.showError('Erro ao obter link de download.')
    });
  }

  onDragEnter(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(true);
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(true);
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'copy';
    }
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    const relatedTarget = event.relatedTarget as HTMLElement;
    if (!relatedTarget || !relatedTarget.closest('.group')) {
      this.isDragging.set(false);
    }
  }

  get files() {
    return this.state().pagination.data;
  }

  get pagination() {
    return this.state().pagination.pagination;
  }

  get isLoading() {
    return this.state().loading;
  }

  get currentPage() {
    return this.pagination.page;
  }

  getFileTypeInfo(contentType: string): { label: string; colorClass: string; icon: string } {
    const type = contentType.toLowerCase();

    if (type.includes('svg')) {
      return {label: 'SVG', colorClass: 'bg-orange-100 text-orange-700', icon: 'vector'};
    }
    if (type.includes('gif')) {
      return {label: 'GIF', colorClass: 'bg-fuchsia-100 text-fuchsia-700', icon: 'image'};
    }
    if (type.includes('image')) {
      const subType = type.split('/')[1]?.toUpperCase().slice(0, 3) || 'IMG';
      return {label: subType === 'JPE' ? 'JPG' : subType, colorClass: 'bg-purple-100 text-purple-700', icon: 'image'};
    }

    if (type.includes('mp4')) {
      return {label: 'MP4', colorClass: 'bg-indigo-100 text-indigo-700', icon: 'video'};
    }
    if (type.includes('webm')) {
      return {label: 'WEBM', colorClass: 'bg-indigo-100 text-indigo-700', icon: 'video'};
    }
    if (type.includes('mkv') || type.includes('x-matroska')) {
      return {label: 'MKV', colorClass: 'bg-slate-800 text-white', icon: 'video'};
    }
    if (type.includes('video')) {
      return {label: 'VID', colorClass: 'bg-indigo-100 text-indigo-700', icon: 'video'};
    }

    if (type.includes('pdf')) {
      return {label: 'PDF', colorClass: 'bg-red-100 text-red-700', icon: 'pdf'};
    }
    if (type.includes('word') || type.includes('document') || type.includes('msword')) {
      return {label: 'DOC', colorClass: 'bg-blue-100 text-blue-700', icon: 'doc'};
    }
    if (type.includes('excel') || type.includes('sheet') || type.includes('csv')) {
      return {label: 'XLS', colorClass: 'bg-emerald-100 text-emerald-700', icon: 'sheet'};
    }

    if (type.includes('json')) {
      return {label: 'JSON', colorClass: 'bg-yellow-100 text-yellow-800', icon: 'code'};
    }
    if (type.includes('xml')) {
      return {label: 'XML', colorClass: 'bg-orange-100 text-orange-800', icon: 'code'};
    }
    if (type.includes('html')) {
      return {label: 'HTML', colorClass: 'bg-orange-50 text-orange-600', icon: 'code'};
    }

    if (type.includes('android') || type.includes('apk')) {
      return {label: 'APK', colorClass: 'bg-green-100 text-green-700', icon: 'android'};
    }
    if (type.includes('zip') || type.includes('compressed') || type.includes('rar')) {
      return {label: 'ZIP', colorClass: 'bg-slate-200 text-slate-700', icon: 'archive'};
    }

    if (type.includes('text')) {
      return {label: 'TXT', colorClass: 'bg-gray-100 text-gray-700', icon: 'text'};
    }

    return {label: 'FILE', colorClass: 'bg-slate-100 text-slate-500', icon: 'file'};
  }
}
