import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {FileMetadata} from '../@types/FileMetadata';
import {Pagination} from '../@types/Pagination';
import {environment} from '../../../environment/environment';

export interface FileState {
  loading: boolean;
  pagination: Pagination<FileMetadata>;
  error: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class FileService {
  private readonly apiUrl = `${environment.baseUrl}/api/files`;
  private http = inject(HttpClient);

  private readonly initialState: FileState = {
    loading: false,
    pagination: {
      data: [],
      pagination: {page: 0, size: 10, totalElements: 0, totalPages: 0, totalSize: 0}
    },
    error: null,
  };

  private readonly fileStateSubject = new BehaviorSubject<FileState>(this.initialState);

  readonly fileState$ = this.fileStateSubject.asObservable();

  private get currentState(): FileState {
    return this.fileStateSubject.getValue();
  }

  list(page = 0, size = 10): void {
    this.fileStateSubject.next({...this.currentState, loading: true, error: null});

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    this.http.get<Pagination<FileMetadata>>(this.apiUrl, {params}).subscribe({
      next: (response) => {
        this.fileStateSubject.next({
          loading: false,
          pagination: response,
          error: null
        });
      },
      error: (err) => {
        this.fileStateSubject.next({
          ...this.currentState,
          loading: false,
          error: 'Erro ao carregar arquivos.'
        });
        console.error(err);
      }
    });
  }

  // AGORA RETORNA OBSERVABLE PARA O COMPONENTE
  upload(files: File[]): Observable<FileMetadata[]> {
    this.fileStateSubject.next({...this.currentState, loading: true, error: null});

    const formData = new FormData();
    for (let i = 0; i < files.length; i++) {
      formData.append('files', files[i]);
    }

    return this.http.post<FileMetadata[]>(this.apiUrl, formData).pipe(
      // 'tap' executa a atualização de estado (efeito colateral)
      tap({
        next: (newFiles) => {
          const current = this.currentState;
          const newFilesSize = newFiles.reduce((acc, curr) => acc + curr.size, 0);

          const newState: FileState = {
            loading: false,
            error: null,
            pagination: {
              ...current.pagination,
              // Adiciona os novos arquivos no início da lista
              data: [...newFiles, ...current.pagination.data],
              pagination: {
                ...current.pagination.pagination,
                totalElements: current.pagination.pagination.totalElements + newFiles.length,
                totalSize: current.pagination.pagination.totalSize + newFilesSize
              }
            }
          };

          this.fileStateSubject.next(newState);
        },
        error: (err) => {
          this.fileStateSubject.next({...this.currentState, loading: false, error: 'Falha no upload.'});
          console.error(err);
        }
      })
    );
  }

  // AGORA RETORNA OBSERVABLE PARA O COMPONENTE
  delete(id: string): Observable<void> {
    const fileToDelete = this.currentState.pagination.data.find(f => f.id === id);

    // Se não achar, lançamos erro para o componente capturar no subscribe
    if (!fileToDelete) {
      return new Observable(observer => observer.error('Arquivo não encontrado localmente'));
    }

    this.fileStateSubject.next({...this.currentState, loading: true, error: null});

    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap({
        next: () => {
          const current = this.currentState;

          const newState: FileState = {
            loading: false,
            error: null,
            pagination: {
              ...current.pagination,
              data: current.pagination.data.filter(f => f.id !== id),
              pagination: {
                ...current.pagination.pagination,
                totalElements: Math.max(0, current.pagination.pagination.totalElements - 1),
                totalSize: Math.max(0, current.pagination.pagination.totalSize - fileToDelete.size)
              }
            }
          };

          this.fileStateSubject.next(newState);
        },
        error: (err) => {
          this.fileStateSubject.next({...this.currentState, loading: false, error: 'Erro ao excluir: ' + err.message});
        }
      })
    );
  }

  getDownloadUrl(id: string): Observable<{ url: string }> {
    return this.http.get<{ url: string }>(`${this.apiUrl}/${id}/download`);
  }
}
